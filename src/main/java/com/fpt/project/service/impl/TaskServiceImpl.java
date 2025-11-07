
package com.fpt.project.service.impl;

import com.fpt.project.constant.TaskStatus;
import com.fpt.project.dto.request.TaskCreateRequest;
import com.fpt.project.dto.request.TaskUpdateAssigneesRequest;
import com.fpt.project.dto.request.TaskUpdateStatusRequest;
import com.fpt.project.dto.response.KanbanBoardResponse;
import com.fpt.project.dto.response.TaskResponse;
import com.fpt.project.dto.response.UserResponse;
import com.fpt.project.entity.Project;
import com.fpt.project.entity.Task;
import com.fpt.project.entity.User;
import com.fpt.project.exception.ApiException;
import com.fpt.project.repository.ProjectRepository;
import com.fpt.project.repository.TaskRepository;
import com.fpt.project.repository.UserRepository;
import com.fpt.project.service.TaskService;
import com.fpt.project.util.SecurityUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class TaskServiceImpl implements TaskService {

    private final TaskRepository taskRepository;
    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;

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

//    @Override
//    public TaskResponse createTask(TaskCreateRequest request) throws ApiException {
//        // Kiểm tra project tồn tại
//        Project project = projectRepository.findById(request.getProjectId())
//                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND.value(), "Không tìm thấy dự án"));
//
//        // Lấy user hiện tại
//
//
//        // Tạo task mới
//        Task task = Task.builder()
//                .title(request.getTitle())
//                .description(request.getDescription())
//                .dueDate(request.getDueDate())
//                .status(TaskStatus.TODO) // Mặc định là TODO
//                .project(project)
//                .createdBy(currentUser)
//                .assignees(new HashSet<>())
//                .build();
//
//        // Thêm assignees nếu có
////        if (request.getAssigneeIds() != null && !request.getAssigneeIds().isEmpty()) {
////            List<User> assignees = new ArrayList<>();
////            for (Integer assigneeId : request.getAssigneeIds()) {
////                userRepository.findById(assigneeId).ifPresent(assignees::add);
////            }
////
////            if (assignees.size() != request.getAssigneeIds().size()) {
//////                throw new ApiException("Một số người được giao không tìm thấy", HttpStatus.BAD_REQUEST);
////                throw new ApiException(HttpStatus.BAD_REQUEST.value(), "Một số người được giao không tìm thấy");
////            }
////            task.setAssignees(new HashSet<>(assignees));
////        }
//
//        Task savedTask = taskRepository.save(task);
//        return mapToTaskResponse(savedTask);
//    }



//    @Override
//    public  TaskResponse updateTaskStatus(Long taskId, TaskUpdateStatusRequest request) throws ApiException{



//    @Override
//    public TaskResponse updateTaskStatus(Integer taskId, TaskUpdateStatusRequest request) throws ApiException{
//        // Kiểm tra task tồn tại
//        Task task = taskRepository.findById(taskId)
////                .orElseThrow(() -> new ApiException("Không tìm thấy nhiệm vụ", HttpStatus.NOT_FOUND));
//                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND.value(), "Không tìm thấy dự án"));
//
//        // Kiểm tra quyền: chỉ người tạo hoặc assignee mới có thể update
//        User currentUser = SecurityUtil.getCurrentUser();
//        boolean hasPermission = task.getCreatedBy().getId() == currentUser.getId() ||
//                task.getAssignees().contains(currentUser);
//
//        if (!hasPermission) {
////            throw new ApiException("Bạn không có quyền cập nhật tác vụ này", HttpStatus.FORBIDDEN);
//            throw new ApiException(HttpStatus.FAILED_DEPENDENCY.value(),"Bạn không có quyền cập nhật tác vụ này");
//        }
//
//        task.setStatus(request.getStatus());
//        Task updatedTask = taskRepository.save(task);
//
//        return mapToTaskResponse(updatedTask);
//    }

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

//    @Override
//    public TaskResponse updateTaskAssignees(Integer taskId, TaskUpdateAssigneesRequest request) throws ApiException{
//        Task task = taskRepository.findById(taskId)
////                .orElseThrow(() -> new ApiException("Không tìm thấy nhiệm vụ", HttpStatus.NOT_FOUND.value()));
//                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND.value(), "Không tìm thấy nhiệm vụ"));
//
//        // Kiểm tra quyền: chỉ người tạo task mới có thể update assignees
//        User currentUser = SecurityUtil.getCurrentUser();
//        if (task.getCreatedBy().getId() != currentUser.getId()) {
////            throw new ApiException("Bạn không có quyền cập nhật người được giao nhiệm vụ này", HttpStatus.FORBIDDEN);
//            throw new ApiException(HttpStatus.FORBIDDEN.value(), "Bạn không có quyền cập nhật người được giao nhiệm vụ này");
//        }
//
//        // Clear assignees hiện tại
//        task.getAssignees().clear();
//
//        // Thêm assignees mới nếu có
//        if (request.getAssigneeIds() != null && !request.getAssigneeIds().isEmpty()) {
//            List<User> assignees = new ArrayList<>();
//            for (Integer assigneeId : request.getAssigneeIds()) {
//                userRepository.findById(assigneeId).ifPresent(assignees::add);
//            }
//
//            if (assignees.size() != request.getAssigneeIds().size()) {
////                throw new ApiException("Một số người được giao không tìm thấy", HttpStatus.BAD_REQUEST);
//                throw new ApiException(HttpStatus.BAD_REQUEST.value(), "Một số người được giao không tìm thấy");
//            }
//            task.setAssignees(new HashSet<>(assignees));
//        }
//
//        Task updatedTask = taskRepository.save(task);
//        return mapToTaskResponse(updatedTask);
//    }
//
//    @Override
//    public void deleteTask(Integer taskId) throws ApiException {
//        Task task = taskRepository.findById(taskId)
////                .orElseThrow(() -> new ApiException("Không tìm thấy nhiệm vụ", HttpStatus.NOT_FOUND));
//                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND.value(), "Không tìm thấy nhiệm vụ"));
//
//        // Kiểm tra quyền: chỉ người tạo mới có thể xóa
//        User currentUser = SecurityUtil.getCurrentUser();
//        if (task.getCreatedBy().getId() != currentUser.getId()) {
////            throw new ApiException("Bạn không có quyền xóa tác vụ này", HttpStatus.FORBIDDEN);
//            throw new ApiException(HttpStatus.FORBIDDEN.value(), "Bạn không có quyền xóa tác vụ này");
//        }
//
//        taskRepository.delete(task);
//    }
//
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