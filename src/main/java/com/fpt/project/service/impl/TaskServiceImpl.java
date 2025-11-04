package com.fpt.project.service.impl;

import com.fpt.project.constant.TaskStatus;
import com.fpt.project.dto.request.TaskRequest;
import com.fpt.project.entity.Project;
import com.fpt.project.entity.Task;
import com.fpt.project.entity.User;
import com.fpt.project.repository.ProjectRepository;
import com.fpt.project.repository.TaskRepository;
import com.fpt.project.repository.UserRepository;
import com.fpt.project.service.TaskService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@Transactional
public class TaskServiceImpl implements TaskService {

    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private UserRepository userRepository;

    /**
     * Lấy danh sách Task mà người dùng hiện tại được assign.
     * Phục vụ cho màn "List Your Tasks Activity".
     */
    @Override
    public List<Task> getTasksForCurrentUser() {
        // Lấy username (email) từ thông tin đăng nhập hiện tại
        String username = SecurityContextHolder.getContext().getAuthentication().getName();

        // Tìm user trong DB
        User currentUser = userRepository.findByEmail(username)
                .orElseThrow(() -> new RuntimeException("User not found with email: " + username));

        // Lấy danh sách task theo user ID (qua quan hệ ManyToMany assignees)
        return taskRepository.findByAssignees_Id(currentUser.getId());
    }

    /**
     * Tạo mới một Task trong project cụ thể.
     */
    @Override
    public Task createTask(TaskRequest taskRequest) {
        // Khởi tạo entity Task
        Task task = new Task();
        task.setTitle(taskRequest.getTitle());
        task.setDescription(taskRequest.getDescription());
        task.setDueDate(taskRequest.getDueDate());
        task.setStatus(TaskStatus.TODO); // Mặc định trạng thái ban đầu

        // Liên kết Project
        Project project = projectRepository.findById(taskRequest.getProjectId())
                .orElseThrow(() -> new RuntimeException("Project not found with ID: " + taskRequest.getProjectId()));
        task.setProject(project);

        // Liên kết danh sách User được giao task
        Set<User> assignees = new HashSet<>();
        for (Long userId : taskRequest.getAssigneeIds()) {
            User assignee = userRepository.findById(userId)
                    .orElseThrow(() -> new RuntimeException("User not found with ID: " + userId));
            assignees.add(assignee);
        }
        task.setAssignees(assignees);

        // Lưu vào DB
        return taskRepository.save(task);
    }
}
