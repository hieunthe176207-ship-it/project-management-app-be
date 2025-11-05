package com.fpt.project.service.impl;

import com.fpt.project.service.TaskService;
import com.fpt.project.entity.Task;
import com.fpt.project.repository.TaskRepository;
import com.fpt.project.dto.response.TaskResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import java.util.stream.Collectors;
import java.util.List;

@Service
public class TaskServiceImpl implements TaskService { // Không báo lỗi nữa

    @Autowired
    private TaskRepository taskRepository;

    @Override
    public List<TaskResponse> getTasksForCurrentUser() {

        // Code lấy ID của bạn (đoạn này đã đúng)
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof Jwt)) {
            throw new RuntimeException("Không thể lấy thông tin người dùng hiện tại.");
        }
        Jwt jwt = (Jwt) authentication.getPrincipal();
        Integer currentUserId = null;

        if (jwt.hasClaim("id")) {
            Number idClaim = (Number) jwt.getClaim("id");
            currentUserId = idClaim.intValue();

        } else if (jwt.getSubject() != null) {
            try {
                currentUserId = Integer.parseInt(jwt.getSubject());
            } catch (NumberFormatException e) {
                throw new RuntimeException("Lỗi token: 'sub' (subject) là email, nhưng không tìm thấy claim 'id'.", e);
            }
        }
        if (currentUserId == null) {
            throw new RuntimeException("Không thể lấy ID người dùng từ JWT token.");
        }

        List<Task> tasksFromDb = taskRepository.findByAssignees_Id(currentUserId);

        return tasksFromDb.stream()
                .map(TaskResponse::new)
                .collect(Collectors.toList());

    }
}