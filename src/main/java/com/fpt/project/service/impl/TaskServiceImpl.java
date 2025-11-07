package com.fpt.project.service.impl;

import com.fpt.project.constant.MemberStatus;
import com.fpt.project.constant.Role;
import com.fpt.project.constant.TaskStatus;
import com.fpt.project.dto.request.CreateTaskRequestDto;
import com.fpt.project.dto.response.SubTaskResponse;
import com.fpt.project.dto.response.TaskDetailResponseDto;
import com.fpt.project.dto.response.UserResponse;
import com.fpt.project.entity.*;
import com.fpt.project.exception.ApiException;
import com.fpt.project.repository.*;
import com.fpt.project.service.TaskService;
import com.fpt.project.dto.response.TaskResponse;
import com.fpt.project.util.SecurityUtil;
import com.fpt.project.util.Util;
import org.checkerframework.checker.units.qual.A;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;

import java.util.Set;
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

    @Autowired
    private SubTaskRepositry subTaskRepositry;

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

        return null;

    }

    @Override
    public Integer addTaskToProject(CreateTaskRequestDto data) throws ApiException {

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
            newTask.getAssignees().add(assignee);
        }

        Task t = taskRepository.save(newTask);
        return t.getId();
    }

    @Override
    public List<TaskResponse> getAllTasksByProjectId(Integer projectId) throws ApiException {
        String email = securityUtil.getEmailRequest();
        User user = userRepository.findByEmail(email);
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ApiException(404, "Dự án không tồn tại."));
        ProjectMember projectMember = projectMemberRepository.findUserByProjectIdAndUserId(project.getId(), user.getId());
        if (projectMember == null) {
            throw new ApiException(400, "Bạn không phải thành viên của dự án này.");
        }

        List<Task> tasks = taskRepository.findByProject_Id(projectId);
        List<TaskResponse> taskForPeoject = tasks.stream()
                .map(t -> {
                    TaskResponse taskdto = new TaskResponse();
                    taskdto.setId(t.getId());
                    taskdto.setTitle(t.getTitle());
                    taskdto.setDescription(t.getDescription());
                    taskdto.setDueDate(t.getDueDate().toString());
                    taskdto.setStatus(t.getStatus().toString());
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

        Task task = taskRepository.findById((long) id)
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
        Task task = taskRepository.findById((long) taskId)
                .orElseThrow(() -> new ApiException(404, "Công việc không tồn tại."));
        ProjectMember projectMember = projectMemberRepository.findUserByProjectIdAndUserId(task.getProject().getId(), user.getId());
        if (projectMember == null) {
            throw new ApiException(400, "Bạn không phải thành viên của dự án này.");
        }
        if(projectMember.getRole() != Role.OWNER ){
            throw new ApiException(400, "Chỉ quản lý hoặc chủ dự án mới có quyền thêm công việc phụ.");
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
        Set<User> assignees = task.getAssignees();
        boolean isAssignee = assignees.stream().anyMatch(u -> u.getId() == user.getId());
        if (!isAssignee) {
            throw new ApiException(400, "Chỉ thành viên được giao công việc mới có quyền cập nhật trạng thái công việc phụ.");
        }
        subTask.setCompleted(completed);
        subTaskRepositry.save(subTask);
    }


}