package com.fpt.project.service.impl;

import com.fpt.project.constant.MemberStatus;
import com.fpt.project.constant.NotificationType;
import com.fpt.project.constant.Role;
import com.fpt.project.constant.TaskStatus;
import com.fpt.project.dto.request.CreateTaskRequestDto;
import com.fpt.project.dto.request.TaskUpdateStatusRequest;
import com.fpt.project.dto.request.UpdateTaskRequest;
import com.fpt.project.dto.response.*;
import com.fpt.project.entity.*;
import com.fpt.project.exception.ApiException;
import com.fpt.project.repository.*;
import com.fpt.project.service.TaskService;
import com.fpt.project.util.SecurityUtil;
import com.fpt.project.util.Util;
import com.google.firebase.messaging.FirebaseMessagingException;
import org.checkerframework.checker.units.qual.A;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

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

    @Autowired
    private SubTaskRepositry subTaskRepositry;

    @Autowired
    private FirebaseService firebaseService;

    @Autowired
    private NotificationRepository notificationRepository;

    @Autowired
    private SimpMessagingTemplate messaging;

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
        String email = securityUtil.getEmailRequest();
        User user = userRepository.findByEmail(email);

        // Kiểm tra task tồn tại
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND.value(), "Không tìm thấy công việc"));

        ProjectMember projectMember = projectMemberRepository.findUserByProjectIdAndUserId(task.getProject().getId(), user.getId());
        if (projectMember == null) {
            throw new ApiException(400, "Bạn không phải thành viên của dự án này.");
        }

        // Kiểm tra xem user có phải là owner hoặc được giao task không
        boolean isOwner = projectMember.getRole() == Role.OWNER;
        boolean isAssignee = task.getAssignees().stream().anyMatch(u -> u.getId() == user.getId());

        if (!isOwner && !isAssignee) {
            throw new ApiException(400, "Chỉ quản lý dự án hoặc người được giao công việc mới có quyền cập nhật trạng thái công việc.");
        }

        List<SubTask> subTasks = subTaskRepositry.findByTask_Id(taskId);
        if(request.getStatus() == TaskStatus.DONE){
            // Kiểm tra tất cả subtask đã hoàn thành chưa
            boolean allSubTasksCompleted = subTasks.stream().allMatch(SubTask::isCompleted);
            if (!allSubTasksCompleted) {
                throw new ApiException(400, "Không thể chuyển công việc sang trạng thái hoàn thành khi còn công việc phụ chưa hoàn thành.");
            }
        }
        // Cập nhật trạng thái task
        task.setStatus(request.getStatus());
        Task updatedTask = taskRepository.save(task);

        return mapToTaskResponse(updatedTask);
    }


    @Override
    public List<TaskResponseDto> getTasksForCurrentUser() {

        // Code lấy ID của bạn (đoạn này đã đúng)
        String email = securityUtil.getEmailRequest();
        User user = userRepository.findByEmail(email);

        List <Task> tasks = taskRepository.findAllAssignedToUser(user.getId());


        return tasks.stream().map(t -> {
            TaskResponseDto taskDto = new TaskResponseDto();
            taskDto.setId(t.getId());
            taskDto.setTitle(t.getTitle());
            taskDto.setDescription(t.getDescription());
            taskDto.setDueDate(t.getDueDate().toString());
            taskDto.setStatus(t.getStatus().toString());
            taskDto.setProjectName(t.getProject().getName());
            return taskDto;
        }).toList();

    }

    @Transactional
    @Override
    public Integer addTaskToProject(CreateTaskRequestDto data) throws ApiException, FirebaseMessagingException {

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
        newTask.setStatus(TaskStatus.TODO);
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
            if(assigneeMember.getStatus() == MemberStatus.REMOVED){
                throw new ApiException(400, "Người được giao " + assignee.getDisplayName() + " đã bị gỡ khỏi dự án.");
            }

            newTask.getAssignees().add(assignee);
        }

        Task t = taskRepository.save(newTask);

        //gửi và lưu notification

        for(User assignee : t.getAssignees()){
            Notification notification = new Notification();
            notification.setTitle("Công việc mới");
            notification.setContent(user.getDisplayName()+ " đã giao việc: " + t.getTitle() + " trong dự án: " + project.getName());
            notification.setType(NotificationType.TASK);
            notification.setTargetId(t.getId());
            notification.setUser(assignee);
            notification.setSender(user);
            //lưu notification
            notificationRepository.save(notification);
            firebaseService.sendToToken(assignee.getTokenFcm(),
                    "Bạn đã được giao một công việc mới: " + t.getTitle(),
                    "Dự án: " + project.getName(),
                    Map.of(
                            "id", String.valueOf(t.getId()),
                            "type", "TASK"
                    )
            );
            messaging.convertAndSend("/topic/notify/" + assignee.getId(),
                    NotificationResponse.builder()
                            .id(notification.getId())
                            .type(notification.getType().toString())
                            .title(notification.getTitle())
                            .content(notification.getContent())
                            .createdAt(notification.getCreatedAt().toString())
                            .isRead(notification.getIsRead())
                            .targetId(notification.getTargetId())
                            .sender(UserResponse.builder()
                                    .displayName(notification.getSender().getDisplayName())
                                    .email(notification.getSender().getEmail())
                                    .avatar(notification.getSender().getAvatar())
                                    .build())
                            .build());
        }
        return t.getId();
    }

    @Override
    public List<ListTaskForProjectResponse> getAllTasksByProjectId(Integer projectId) throws ApiException {
        String email = securityUtil.getEmailRequest();
        User user = userRepository.findByEmail(email);
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ApiException(404, "Dự án không tồn tại."));
        ProjectMember projectMember = projectMemberRepository.findUserByProjectIdAndUserId(project.getId(), user.getId());
        if (projectMember == null) {
            throw new ApiException(400, "Bạn không phải thành viên của dự án này.");
        }

        List<Task> tasks = taskRepository.findByProject_Id(projectId);
        List<ListTaskForProjectResponse> taskForPeoject = tasks.stream()
                .map(t -> {
                    ListTaskForProjectResponse taskdto = new ListTaskForProjectResponse();
                    taskdto.setId(t.getId());
                    taskdto.setTitle(t.getTitle());
                    taskdto.setDescription(t.getDescription());
                    taskdto.setDueDate(t.getDueDate().toString());
                    taskdto.setStatus(t.getStatus());
                    taskdto.setAssignees(t.getAssignees().stream()
                            .map(u -> {
                                ProjectMember pm = projectMemberRepository.findUserByProjectIdAndUserId(projectId, u.getId());
                                if(pm.getStatus() != MemberStatus.REMOVED){
                                    UserResponse userdto = new UserResponse();
                                    userdto.setId(u.getId());
                                    userdto.setDisplayName(u.getDisplayName());
                                    userdto.setEmail(u.getEmail());
                                    return userdto;
                                }
                               return null;
                            })
                            .collect(Collectors.toSet())
                    );
                    return taskdto;
                })
                .toList();


        return taskForPeoject;
    }

    @Override
    public TaskDetailResponseDto getTaskDetailById(int id) throws ApiException {
        String email = securityUtil.getEmailRequest();
        User user = userRepository.findByEmail(email);

        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new ApiException(404, "Công việc không tồn tại."));
        ProjectMember projectMember = projectMemberRepository.findUserByProjectIdAndUserId(task.getProject().getId(), user.getId());
        if (projectMember == null) {
            throw new ApiException(400, "Bạn không phải thành viên của dự án này.");
        }
        return TaskDetailResponseDto.builder()
                .id(task.getId())
                .title(task.getTitle())
                .description(task.getDescription())
                .dueDate(task.getDueDate().toString())
                .status(task.getStatus().toString())
                .projectId(task.getProject().getId())
                .assignees(task.getAssignees().stream()
                        .map(u -> {
                            ProjectMember pm = projectMemberRepository.findUserByProjectIdAndUserId(task.getProject().getId(), u.getId());
                            if(pm.getStatus() != MemberStatus.REMOVED){
                                UserResponse userdto = new UserResponse();
                                userdto.setId(u.getId());
                                userdto.setDisplayName(u.getDisplayName());
                                userdto.setEmail(u.getEmail());
                                userdto.setAvatar(u.getAvatar());
                                return userdto;
                            }
                            return null;
                        })
                        .collect(Collectors.toList())
                )
                .subTasks(
                        subTaskRepositry.findByTask_Id(task.getId()).stream()
                                .map(st -> com.fpt.project.dto.response.SubTaskResponse.builder()
                                        .id(st.getId())
                                        .title(st.getTitle())
                                        .completed(st.isCompleted())
                                        .build()
                                )
                                .collect(Collectors.toList())
                )
                .build();
    }

    @Override
    public SubTaskResponse createSubTask(int taskId, String title) throws ApiException {
        String email = securityUtil.getEmailRequest();
        User user = userRepository.findByEmail(email);
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new ApiException(404, "Công việc không tồn tại."));
        ProjectMember projectMember = projectMemberRepository.findUserByProjectIdAndUserId(task.getProject().getId(), user.getId());
        if (projectMember == null) {
            throw new ApiException(400, "Bạn không phải thành viên của dự án này.");
        }

        // Kiểm tra xem user có phải là owner hoặc được giao task không
        boolean isOwner = projectMember.getRole() == Role.OWNER;
        boolean isAssignee = task.getAssignees().stream().anyMatch(u -> u.getId() == user.getId());

        if (!isOwner && !isAssignee) {
            throw new ApiException(400, "Chỉ quản lý dự án hoặc người được giao công việc mới có quyền thêm công việc phụ.");
        }

        SubTask newSubTask = new SubTask();
        newSubTask.setTitle(title);
        newSubTask.setTask(task);
        newSubTask.setCompleted(false);

        int id = subTaskRepositry.save(newSubTask).getId();
        SubTaskResponse subTaskResponse = new SubTaskResponse();
        subTaskResponse.setId(id);
        subTaskResponse.setTitle(title);
        subTaskResponse.setCompleted(false);
        return subTaskResponse;
    }

    @Override
    public void updateSubTaskComplete(int subTaskId, boolean completed) throws ApiException {
        String email = securityUtil.getEmailRequest();
        User user = userRepository.findByEmail(email);
        SubTask subTask = subTaskRepositry.findById(subTaskId)
                .orElseThrow(() -> new ApiException(404, "Công việc phụ không tồn tại."));
        Task task = subTask.getTask();
        ProjectMember projectMember = projectMemberRepository.findUserByProjectIdAndUserId(task.getProject().getId(), user.getId());
        if (projectMember == null) {
            throw new ApiException(400, "Bạn không phải thành viên của dự án này.");
        }

        // Kiểm tra xem user có phải là owner hoặc được giao task không
        boolean isOwner = projectMember.getRole() == Role.OWNER;
        boolean isAssignee = task.getAssignees().stream().anyMatch(u -> u.getId() == user.getId());

        if (!isOwner && !isAssignee) {
            throw new ApiException(400, "Chỉ quản lý dự án hoặc người được giao công việc mới có quyền cập nhật trạng thái công việc phụ.");
        }

        subTask.setCompleted(completed);
        subTaskRepositry.save(subTask);
    }

    @Override
    public Integer updateTask(UpdateTaskRequest data) throws ApiException, FirebaseMessagingException {
        String email = securityUtil.getEmailRequest();
        User user = userRepository.findByEmail(email);
        Task task = taskRepository.findById(data.getId())
                .orElseThrow(() -> new ApiException(404, "Công việc không tồn tại."));
        ProjectMember projectMember = projectMemberRepository.findUserByProjectIdAndUserId(task.getProject().getId(), user.getId());
        if (projectMember == null) {
            throw new ApiException(400, "Bạn không phải thành viên của dự án này.");
        }
        if(projectMember.getRole() != Role.OWNER){
            throw new ApiException(400, "Chỉ quản lý hoặc chủ dự án mới có quyền cập nhật công việc.");
        }

        // Lưu danh sách assignees cũ để so sánh
        Set<User> oldAssignees = new HashSet<>(task.getAssignees());

        task.setTitle(data.getTitle());
        task.setDescription(data.getDescription());
        task.setDueDate(Util.parseToLocalDate(data.getDueDate()));

        //update assignees
        Set<User> newAssignees = new HashSet<>();
        for (Integer assigneeId : data.getAssigneeIds()) {
            User assignee = userRepository.findById(assigneeId)
                    .orElseThrow(() -> new ApiException(404, "Người được giao với ID " + assigneeId + " không tồn tại."));
            ProjectMember assigneeMember = projectMemberRepository.findUserByProjectIdAndUserId(task.getProject().getId(), assigneeId);
            if (assigneeMember == null) {
                throw new ApiException(400, "Người được giao " + assignee.getDisplayName() + " không phải thành viên của dự án.");
            }
            newAssignees.add(assignee);
        }
        task.setAssignees(newAssignees);
        Task updatedTask = taskRepository.save(task);

        // Tìm những người được giao mới (không có trong danh sách cũ)
        Set<User> newlyAssignedUsers = new HashSet<>(newAssignees);
        newlyAssignedUsers.removeAll(oldAssignees);

        // Gửi thông báo cho những người được giao mới
        for(User newAssignee : newlyAssignedUsers) {
            Notification notification = new Notification();
            notification.setTitle("Công việc mới");
            notification.setContent(user.getDisplayName()+ " đã giao việc: " + updatedTask.getTitle() + " trong dự án: " + task.getProject().getName());
            notification.setType(NotificationType.TASK);
            notification.setTargetId(updatedTask.getId());
            notification.setUser(newAssignee);
            notification.setSender(user);

            // Lưu notification
            notificationRepository.save(notification);

            messaging.convertAndSend("/topic/notify/" + newAssignee.getId(),
                    NotificationResponse.builder()
                            .id(notification.getId())
                            .type(notification.getType().toString())
                            .title(notification.getTitle())
                            .content(notification.getContent())
                            .createdAt(notification.getCreatedAt().toString())
                            .isRead(notification.getIsRead())
                            .targetId(notification.getTargetId())
                            .sender(UserResponse.builder()
                                    .displayName(notification.getSender().getDisplayName())
                                    .email(notification.getSender().getEmail())
                                    .avatar(notification.getSender().getAvatar())
                                    .build())
                            .build());

            // Gửi Firebase notification
            firebaseService.sendToToken(newAssignee.getTokenFcm(),
                    "Bạn đã được giao một công việc: " + updatedTask.getTitle(),
                    "Dự án: " + task.getProject().getName(),
                    Map.of(
                            "id", String.valueOf(updatedTask.getId()),
                            "type", "TASK"
                    )
            );

        }

        return task.getId();
    }

    @Override
    public void deleteTask(int taskId) throws ApiException, FirebaseMessagingException {
        String email = securityUtil.getEmailRequest();
        User user = userRepository.findByEmail(email);
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new ApiException(404, "Công việc không tồn tại."));
        ProjectMember projectMember = projectMemberRepository.findUserByProjectIdAndUserId(task.getProject().getId(), user.getId());
        if (projectMember == null) {
            throw new ApiException(400, "Bạn không phải thành viên của dự án này.");
        }
        if(projectMember.getRole() != Role.OWNER){
            throw new ApiException(400, "Chỉ quản lý hoặc chủ dự án mới có quyền xóa công việc.");
        }

        // Gửi thông báo cho các thành viên được assign trước khi xóa task
        for(User assignee : task.getAssignees()) {
            Notification notification = new Notification();
            notification.setTitle("Công việc đã bị xóa");
            notification.setContent("Công việc: " + task.getTitle() + " trong dự án: " + task.getProject().getName() + " đã bị xóa");
            notification.setType(NotificationType.TASK_REMOVED);
            notification.setTargetId(task.getId());
            notification.setUser(assignee);
            notification.setSender(user);

            // Lưu notification
            notificationRepository.save(notification);

            messaging.convertAndSend("/topic/notify/" + assignee.getId(),
                    NotificationResponse.builder()
                            .id(notification.getId())
                            .type(notification.getType().toString())
                            .title(notification.getTitle())
                            .content(notification.getContent())
                            .createdAt(notification.getCreatedAt().toString())
                            .isRead(notification.getIsRead())
                            .targetId(notification.getTargetId())
                            .sender(UserResponse.builder()
                                    .displayName(notification.getSender().getDisplayName())
                                    .email(notification.getSender().getEmail())
                                    .avatar(notification.getSender().getAvatar())
                                    .build())
                            .build());

            // Gửi Firebase notification
            firebaseService.sendToToken(assignee.getTokenFcm(),
                    "Công việc đã bị xóa: " + task.getTitle(),
                    "Dự án: " + task.getProject().getName(),
                    Map.of(
                            "id", String.valueOf(task.getId()),
                            "type", "TASK_REMOVED"
                    )
            );
        }

        taskRepository.delete(task);
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