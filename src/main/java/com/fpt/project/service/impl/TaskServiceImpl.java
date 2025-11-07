package com.fpt.project.service.impl;

import com.fpt.project.constant.Role;
import com.fpt.project.constant.TaskStatus;
import com.fpt.project.dto.request.CreateTaskRequestDto;
import com.fpt.project.dto.request.TaskUpdateStatusRequest;
import com.fpt.project.dto.response.KanbanBoardResponse;
import com.fpt.project.dto.response.UserResponse;
import com.fpt.project.entity.Project;
import com.fpt.project.entity.ProjectMember;
import com.fpt.project.entity.User;
import com.fpt.project.exception.ApiException;
import com.fpt.project.repository.ProjectMemberRepository;
import com.fpt.project.repository.ProjectRepository;
import com.fpt.project.repository.TaskRepository;
import com.fpt.project.repository.UserRepository;
import com.fpt.project.service.TaskService;
import com.fpt.project.entity.Task;
import com.fpt.project.repository.TaskRepository;
import com.fpt.project.dto.response.TaskResponse;
import com.fpt.project.util.SecurityUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import com.fpt.project.util.Util;
import org.checkerframework.checker.units.qual.A;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class TaskServiceImpl implements TaskService {

    private final TaskRepository taskRepository;
    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;
    private final SecurityUtil securityUtil;
    private final ProjectMemberRepository projectMemberRepository;

    @Override
    @Transactional(readOnly = true)
    public KanbanBoardResponse getKanbanBoard(Integer projectId) throws ApiException {
        // Kiểm tra project tồn tại
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND.value(), "Không tìm thấy dự án"));


        // Lấy tất cả tasks của project với assignees
        List<Task> tasks = taskRepository.findByProjectIdWithAssignees(projectId);

        // Nhóm tasks theo status
        Map<TaskStatus, List<TaskResponse>> tasksByStatus = new EnumMap<>(TaskStatus.class);

        // Khởi tạo tất cả status
        for (TaskStatus status : TaskStatus.values()) {
            tasksByStatus.put(status, new ArrayList<>());
        }

        // Phân loại tasks
        tasks.forEach(task -> {
            TaskResponse taskResponse = mapToTaskResponse(task);
            tasksByStatus.get(task.getStatus()).add(taskResponse);
        });

        return KanbanBoardResponse.builder()
                .projectId(project.getId())
                .projectName(project.getName())
                .todoTasks(tasksByStatus.get(TaskStatus.TODO))
                .inProgressTasks(tasksByStatus.get(TaskStatus.IN_PROGRESS))
                .inReviewTasks(tasksByStatus.get(TaskStatus.IN_REVIEW))
                .doneTasks(tasksByStatus.get(TaskStatus.DONE))
                .build();
    }

    @Override
    public TaskResponse updateTaskStatus(Integer taskId, TaskUpdateStatusRequest request) throws ApiException {
        // Kiểm tra task tồn tại
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND.value(), "Không tìm thấy dự án"));

        // Bỏ kiểm tra quyền

        // Cập nhật trạng thái task
        task.setStatus(request.getStatus());
        Task updatedTask = taskRepository.save(task);

        return mapToTaskResponse(updatedTask);
    }
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



    private TaskResponse mapToTaskResponse(Task task) {
        List<UserResponse> assigneeResponses = task.getAssignees().stream()
                .map(this::mapToUserResponse)
                .collect(Collectors.toList());

        return TaskResponse.builder()
                .id((long)task.getId())
                .title(task.getTitle())
                .description(task.getDescription())
                .dueDate(task.getDueDate().toString())
                .status(task.getStatus())
                .projectId((long)task.getProject().getId())
                .createdBy(mapToUserResponse(task.getCreatedBy()))
                .assignees(assigneeResponses)
                .createdAt(task.getCreatedAt().toString())
                .updatedAt(task.getUpdatedAt().toString())
                .build();
    }
    //
    private UserResponse mapToUserResponse(User user) {
        return UserResponse.builder()
                .id((int)user.getId())
                .email(user.getEmail())
                .displayName(user.getDisplayName())
                .avatar(user.getAvatar())
                .build();
    }
}