package com.fpt.project.service.impl;

import com.fpt.project.constant.Role;
import com.fpt.project.dto.request.CreateTaskRequestDto;
import com.fpt.project.entity.Project;
import com.fpt.project.entity.ProjectMember;
import com.fpt.project.entity.User;
import com.fpt.project.exception.ApiException;
import com.fpt.project.repository.ProjectMemberRepository;
import com.fpt.project.repository.ProjectRepository;
import com.fpt.project.repository.UserRepository;
import com.fpt.project.service.TaskService;
import com.fpt.project.entity.Task;
import com.fpt.project.repository.TaskRepository;
import com.fpt.project.dto.response.TaskResponse;
import com.fpt.project.util.SecurityUtil;
import com.fpt.project.util.Util;
import org.checkerframework.checker.units.qual.A;
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

    @Autowired
    private ProjectMemberRepository projectMemberRepository;

    @Autowired
    private SecurityUtil securityUtil;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ProjectRepository projectRepository;

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

    @Override
    public void addTaskToProject(CreateTaskRequestDto data) throws ApiException {

        String email = securityUtil.getEmailRequest();

        User user = userRepository.findByEmail(email);

        Project project = projectRepository.findById(data.getProjectId())
                .orElseThrow(() -> new ApiException(404, "Dự án không tồn tại."));
        ProjectMember projectMember = projectMemberRepository.findUserByProjectIdAndUserId(project.getId(), user.getId());

        if (projectMember == null) {
            throw new ApiException(400, "Bạn không phải thành viên của dự án này.");
        }
        if(projectMember.getRole() != Role.OWNER){
            throw new ApiException(400, "Chỉ quản lý dự án mới có quyền thêm công việc.");
        }

        //compare due date with project end date
        if(Util.parseToLocalDate(data.getDueDate()).isAfter(project.getDeadline())){
            throw new ApiException(400, "Ngày hết hạn công việc không được sau ngày kết thúc dự án.");
        }

        //compare due date with project now
        if(Util.parseToLocalDate(data.getDueDate()).isBefore(Util.getCurrentLocalDate())){
            throw new ApiException(400, "Ngày hết hạn công việc không được trước ngày hiện tại.");
        }
        Task newTask = new Task();
        newTask.setTitle(data.getTitle());
        newTask.setDescription(data.getDescription());
        newTask.setDueDate(Util.parseToLocalDate(data.getDueDate()));
        newTask.setProject(project);
        newTask.setCreatedBy(user);

        for (Integer assigneeId : data.getAssigneeIds()) {
            User assignee = userRepository.findById(assigneeId)
                    .orElseThrow(() -> new ApiException(404, "Người được giao với ID " + assigneeId + " không tồn tại."));
            ProjectMember assigneeMember = projectMemberRepository.findUserByProjectIdAndUserId(project.getId(), assigneeId);
            if (assigneeMember == null) {
                throw new ApiException(400, "Người được giao " + assignee.getDisplayName() + " không phải thành viên của dự án.");
            }
            newTask.getAssignees().add(assignee);
        }

        taskRepository.save(newTask);
    }


}