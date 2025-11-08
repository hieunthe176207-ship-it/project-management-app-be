package com.fpt.project.service.impl;

import com.fpt.project.constant.JoinStatus;
import com.fpt.project.constant.MemberStatus;
import com.fpt.project.constant.NotificationType;
import com.fpt.project.constant.Role;
import com.fpt.project.dto.PageResponse;
import com.fpt.project.dto.request.ProjectCreateRequest;
import com.fpt.project.dto.request.UpdateProjectRequest;
import com.fpt.project.dto.response.*;
import com.fpt.project.entity.*;
import com.fpt.project.exception.ApiException;
import com.fpt.project.repository.*;
import com.fpt.project.service.ProjectService;
import com.fpt.project.util.SecurityUtil;
import com.fpt.project.util.Util;
import com.google.firebase.messaging.FirebaseMessagingException;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProjectServiecImpl implements ProjectService {

    private final ProjectRepository projectRepository;
    private final SecurityUtil securityUtil;
    private final UserRepository userRepository;
    private final ProjectMemberRepository projectMemberRepository;
    private final ChatGroupRepository chatGroupRepository;
    private final JoinRequestRepository joinRequestRepository;
    private final TaskRepository taskRepository;
    private final FirebaseService firebaseService;
    private final NotificationRepository notificationRepository;
    private final SimpMessagingTemplate messaging;

    @Override
    @Transactional
    public void saveProject(ProjectCreateRequest projectCreateRequest) throws ApiException {
        String email = securityUtil.getEmailRequest();
        User user = userRepository.findByEmail(email);
        if (user == null) {
            throw new ApiException(400, "Tài khoản không tồn tại");
        }

        //compare deadline with today
        if (Util.parseToLocalDate(projectCreateRequest.getDeadline()).isBefore(Util.getCurrentLocalDate())) {
            throw new ApiException(400, "Deadline phải lớn hơn hoặc bằng ngày hiện tại");
        }

        //is Public is 0 or 1
        if (projectCreateRequest.getIsPublic() != 0 && projectCreateRequest.getIsPublic() != 1) {
            throw new ApiException(400, "Dữ liệu công khai phải là 0 hoặc 1");
        }

        Project project = Project.builder()
                .name(projectCreateRequest.getName())
                .description(projectCreateRequest.getDescription())
                .createdBy(user)
                .isPublic(projectCreateRequest.getIsPublic())
                .deadline(Util.parseToLocalDate(projectCreateRequest.getDeadline()))
                .build();



        project = projectRepository.save(project);

        ChatGroup chatGroup = new ChatGroup();
        chatGroup.setName(projectCreateRequest.getName() + "-GroupChat");
        chatGroup.setProject(project);
        chatGroupRepository.save(chatGroup);

        ProjectMember projectMember = new ProjectMember();
        projectMember.setProject(project);
        projectMember.setUser(user);
        projectMember.setStatus(MemberStatus.ACTIVE);
        projectMember.setRole(Role.OWNER);

        projectMemberRepository.save(projectMember);
    }


    // Láya toàn bộ dự án của nguời đang yêu cầu
    @Override
    public List<ProjectResponseDto> findProjectByUser() throws ApiException {
        String email = securityUtil.getEmailRequest();

        // Gợi ý: nếu có method fetch-join để tránh N+1 thì dùng:
        // List<Project> projects = projectRepository.findAllByUserEmailFetchMembers(email);
        List<Project> projects = projectRepository.findAllByUserEmail(email);



        return projects.stream()
                .map(project -> {
                    int allTask = taskRepository.countAllTaskFromProject(project.getId());
                    int completedTask = taskRepository.countCompletedTaskFromProject(project.getId());
                    String progess = completedTask+"/"+allTask;

                    //check dealine with today
                    LocalDate today = LocalDate.now();
                    boolean isLate = project.getDeadline() != null && project.getDeadline().isBefore(today);
                    // Lấy danh sách member đang ACTIVE
                    List<UserResponse> activeMembers = project.getProjectMembers() == null
                            ? List.of()
                            : project.getProjectMembers().stream()
                            .filter(pm -> pm.getStatus() == MemberStatus.ACTIVE)
                            .map(pm -> UserResponse.builder()
                                    .displayName(pm.getUser().getDisplayName())
                                    .email(pm.getUser().getEmail())
                                    .avatar(pm.getUser().getAvatar())
                                    .build())
                            .toList();

                    User owner = project.getCreatedBy();

                    return ProjectResponseDto.builder()
                            .id(project.getId())
                            .progress(progess)
                            .isLate(isLate)
                            .name(project.getName())
                            .description(project.getDescription())
                            .deadline(project.getDeadline() != null ? project.getDeadline().toString() : null)
                            .createdBy(UserResponse.builder()
                                    .displayName(owner != null ? owner.getDisplayName() : null)
                                    .email(owner != null ? owner.getEmail() : null)
                                    .avatar(owner != null ? owner.getAvatar() : null)
                                    .build())
                            .members(activeMembers)
                            .build();
                })
                .toList();
    }

    @Override
    public ProjectResponseDto getProjectById(Integer projectId) throws ApiException {
        String email = securityUtil.getEmailRequest();
        User user = userRepository.findByEmail(email);

        ProjectMember projectMember = projectMemberRepository
                .findUserByProjectIdAndUserId(projectId, user.getId());
        if (projectMember == null) {
            throw new ApiException(403, "Bạn không có quyền truy cập dự án này");
        }
        if (projectMember.getStatus() == MemberStatus.REMOVED) {
            throw new ApiException(403, "Bạn đã bị xóa khỏi dự án này");
        }

        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ApiException(404, "Project not found"));

        int countJoinRequest = joinRequestRepository.countRecordsPendingByProjectId(projectId);
        User owner = project.getCreatedBy();

        int allTask = taskRepository.countAllTaskFromProject(project.getId());
        int completedTask = taskRepository.countCompletedTaskFromProject(project.getId());
        String progess = completedTask+"/"+allTask;

        //check dealine with today
        LocalDate today = LocalDate.now();
        boolean isLate = project.getDeadline() != null && project.getDeadline().isBefore(today);


        List<UserResponse> activeMembers = project.getProjectMembers().stream()
                .filter(pm -> pm.getStatus() == MemberStatus.ACTIVE)
                .map(pm -> UserResponse.builder()
                        .displayName(pm.getUser().getDisplayName())
                        .email(pm.getUser().getEmail())
                        .avatar(pm.getUser().getAvatar())
                        .build())
                .toList();

        return ProjectResponseDto.builder()
                .id(project.getId())
                .progress(progess)
                .isLate(isLate)
                .name(project.getName())
                .isPublic(project.getIsPublic())
                .description(project.getDescription())
                .deadline(project.getDeadline().toString())
                .countJoinRequest(countJoinRequest)
                .createdBy(UserResponse.builder()
                        .displayName(owner.getDisplayName())
                        .email(owner.getEmail())
                        .avatar(owner.getAvatar())
                        .build())
                .members(activeMembers)
                .build();
    }

    @Transactional
    @Override
    public void addMembersToProject(Integer projectId, List<Integer> userIds) throws ApiException, FirebaseMessagingException {
        String email = securityUtil.getEmailRequest();
        User currentUser = userRepository.findByEmail(email);
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ApiException( 400, "Project not found"));
        ProjectMember projectMember = projectMemberRepository.findMemberByProjectId(projectId, currentUser.getId());
        if (projectMember.getRole() != Role.OWNER) {
            throw new ApiException(403, "Bạn không có quyền thêm thành viên vào dự án này");
        }
        if(projectMember.getStatus() == MemberStatus.REMOVED){
            throw new ApiException(403, "Bạn đã bị xóa khỏi dự án này");
        }

        for (Integer userId : userIds) {
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new ApiException(404, "Không tìm thấy người dùng: " + userId));
            ProjectMember existingMember = projectMemberRepository.findMemberByProjectId(projectId, userId);
            if (existingMember != null) {
                if (existingMember.getStatus() == MemberStatus.ACTIVE) {
                    throw new ApiException(400, "Người dùng đã là thành viên: " + userId);
                } else if (existingMember.getStatus() == MemberStatus.REMOVED) {

                    Notification notification = new Notification();
                    notification.setTitle("Bạn đã được thêm lại vào dự án: " + project.getName());
                    notification.setUser(user);
                    notification.setContent("Người dùng " + currentUser.getDisplayName() + " đã thêm bạn vào dự án " + project.getName());
                    notification.setSender(currentUser);
                    notification.setTargetId(project.getId());
                    notification.setType(NotificationType.PROJECT);
                    notificationRepository.save(notification);


                    existingMember.setStatus(MemberStatus.ACTIVE);
                    projectMemberRepository.save(existingMember);

                    messaging.convertAndSend("/topic/notify/" + user.getId(),
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
                    firebaseService.sendToToken(
                            user.getTokenFcm(),
                            "Bạn đã được thêm lại vào dự án: " + project.getName(),
                            currentUser.getDisplayName() + " đã thêm bạn vào dự án " + project.getName(),
                            Map.of(
                                    "id", String.valueOf(project.getId()),
                                    "type", "PROJECT"
                            )
                    );
                }
            } else {
                ProjectMember newMember = new ProjectMember();
                newMember.setProject(project);
                newMember.setUser(user);
                newMember.setRole(Role.MEMBER);
                newMember.setStatus(MemberStatus.ACTIVE);

                Notification notification = new Notification();
                notification.setTitle("Tham gia dự án mới");
                notification.setUser(user);
                notification.setContent("Người dùng " + currentUser.getDisplayName() + " đã thêm bạn vào dự án " + project.getName());
                notification.setSender(currentUser);
                notification.setTargetId(project.getId());
                notification.setType(NotificationType.PROJECT);
                notificationRepository.save(notification);
                projectMemberRepository.save(newMember);

                messaging.convertAndSend("/topic/notify/" + user.getId(),
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
                firebaseService.sendToToken(
                        user.getTokenFcm(),
                        "Bạn đã được thêm lại vào dự án: " + project.getName(),
                        currentUser.getDisplayName() + " đã thêm bạn vào dự án " + project.getName(),
                        Map.of(
                                "id", String.valueOf(project.getId()),
                                "type", "PROJECT"
                        )
                );
            }
        }
    }


    @Override
    public List<UserResponse> getUsersByProjectId(Integer projectId) throws ApiException {
        String email = securityUtil.getEmailRequest();
        User user = userRepository.findByEmail(email);
        ProjectMember projectMember = projectMemberRepository.findUserByProjectIdAndUserId(projectId, user.getId());
        if (projectMember == null) {
            throw new ApiException(403, "Bạn không có quyền truy cập dự án này");
        }
        if(projectMember.getStatus() == MemberStatus.REMOVED){
            throw new ApiException(403, "Bạn đã bị xóa khỏi dự án này");
        }
        return projectRepository.findUsersByIdProject(projectId);
    }

    @Override
    public List<ProjectResponseDto> getAllPublicProjects() throws ApiException {
        String email = securityUtil.getEmailRequest();
        User user = userRepository.findByEmail(email);
        List<Project> projects = projectRepository.findAllPublicProjectsNotJoinedByUser(
                user.getId()
        );

        return projects.stream().map(project -> {
            int allTask = taskRepository.countAllTaskFromProject(project.getId());
            int completedTask = taskRepository.countCompletedTaskFromProject(project.getId());
            String progess = completedTask+"/"+allTask;

            //check dealine with today
            LocalDate today = LocalDate.now();
            boolean isLate = project.getDeadline() != null && project.getDeadline().isBefore(today);
            return ProjectResponseDto.builder()
                    .id(project.getId())
                    .progress(progess)
                    .isLate(isLate)
                    .name(project.getName())
                    .description(project.getDescription())
                    .deadline(project.getDeadline().toString())
                    .createdBy(UserResponse.builder()
                            .displayName(project.getCreatedBy().getDisplayName())
                            .email(project.getCreatedBy().getEmail())
                            .avatar(project.getCreatedBy().getAvatar())
                            .build())
                    .members(project.getProjectMembers().stream().map(p -> UserResponse.builder()
                            .displayName(p.getUser().getDisplayName())
                            .email(p.getUser().getEmail())
                            .avatar(p.getUser().getAvatar())
                            .build()).toList())
                    .build();
        }).toList();
    }

    @Transactional
    @Override
    public void requestJoinPublicProject(Integer projectId) throws ApiException, FirebaseMessagingException {
        String email = securityUtil.getEmailRequest();
        User user = userRepository.findByEmail(email);
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ApiException(404, "Không tìm thấy dự án"));
        if (project.getIsPublic() == 0) {
            throw new ApiException(400, "Dự án này không phải là dự án công khai");
        }

        // Check if user is already a send join request
        JoinRequest existingRequest = joinRequestRepository.findByUserIdAndProjectId(user.getId(), projectId);

        if (existingRequest != null) {
            if (existingRequest.getStatus() == JoinStatus.PENDING) {
                throw new ApiException(400, "Bạn đã gửi yêu cầu tham gia dự án này");
            }
            else if (existingRequest.getStatus() == JoinStatus.APPROVED) {
               ProjectMember existingMember = projectMemberRepository.findUserByProjectIdAndUserId(projectId, user.getId());
               if (existingMember != null && existingMember.getStatus() == MemberStatus.ACTIVE) {
                   // đang là thành viên
                   throw new ApiException(400, "Bạn đã là thành viên của dự án này");

                   //là thành viên nhưng bị xóa
               } else if (existingMember != null && existingMember.getStatus() == MemberStatus.REMOVED) {
                   JoinRequest joinRequest= new JoinRequest();
                   joinRequest.setProject(project);
                   joinRequest.setUser(user);
                   joinRequest.setStatus(JoinStatus.PENDING);
                   joinRequestRepository.save(joinRequest);

                   Notification notification = new Notification();
                   notification.setTitle("Yêu cầu tham gia dự án");
                   notification.setUser(project.getCreatedBy());
                   notification.setContent(user.getDisplayName()+ " đã gửi lại yêu cầu tham gia dự án: " + project.getName());
                   notification.setSender(user);
                   notification.setTargetId(project.getId());
                   notification.setType(NotificationType.REQUEST_JOIN);
                   notificationRepository.save(notification);

                   messaging.convertAndSend("/topic/notify/" + project.getCreatedBy().getId(),
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

                   firebaseService.sendToToken(
                           project.getCreatedBy().getTokenFcm(),
                           "Yêu cầu tham gia dự án",
                           user.getDisplayName()+ " đã gửi lại yêu cầu tham gia dự án: " + project.getName(),
                           Map.of(
                                   "id", String.valueOf(project.getId()),
                                   "type", "REQUEST_JOIN"
                           )
                   );
                   return;
               }
            }
        }

        Notification notification = new Notification();
        notification.setTitle("Yêu cầu tham gia dự án");
        notification.setUser(project.getCreatedBy());
        notification.setContent(user.getDisplayName()+ " đã gửi lại yêu cầu tham gia dự án: " + project.getName());
        notification.setSender(user);
        notification.setTargetId(project.getId());
        notification.setType(NotificationType.REQUEST_JOIN);
        notificationRepository.save(notification);

        JoinRequest joinRequest= new JoinRequest();
        joinRequest.setProject(project);
        joinRequest.setUser(user);
        joinRequest.setStatus(JoinStatus.PENDING);
        joinRequestRepository.save(joinRequest);

        messaging.convertAndSend("/topic/notify/" + project.getCreatedBy().getId(),
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
        firebaseService.sendToToken(
                project.getCreatedBy().getTokenFcm(),
                "Yêu cầu tham gia dự án",
                user.getDisplayName()+ " đã gửi lại yêu cầu tham gia dự án: " + project.getName(),
                Map.of(
                        "id", String.valueOf(project.getId()),
                        "type", "REQUEST_JOIN"
                )
        );
    }

    @Override
    public List<UserResponse> getPendingJoinRequests(Integer projectId) throws ApiException {
        String email = securityUtil.getEmailRequest();
        User user = userRepository.findByEmail(email);
        ProjectMember projectMember = projectMemberRepository.findUserByProjectIdAndUserId(projectId, user.getId());
        if(projectMember.getStatus() == MemberStatus.REMOVED){
            throw new ApiException(403, "Bạn đã bị xóa khỏi dự án này");
        }

        if (projectMember == null || projectMember.getRole() != Role.OWNER) {
            throw new ApiException(403, "Bạn không có quyền xem yêu cầu tham gia dự án này");
        }

        List<User> users = joinRequestRepository.findUsersByProjectIdAndStatus(projectId, JoinStatus.PENDING);
        if (users != null && !users.isEmpty()) {
            return users.stream().map(u -> UserResponse.builder()
                    .id(u.getId())
                    .displayName(u.getDisplayName())
                    .email(u.getEmail())
                    .avatar(u.getAvatar())
                    .build()).toList();
        }
        return List.of();
    }

    @Transactional
    @Override
    public void handleJoinRequest(Integer projectId, Integer userId, boolean isApproved) throws ApiException, FirebaseMessagingException {
       String email = securityUtil.getEmailRequest();
       User currentUser = userRepository.findByEmail(email);
        Notification notification = new Notification();
       ProjectMember member = projectMemberRepository.findUserByProjectIdAndUserId(projectId, currentUser.getId());
       if (member == null || member.getRole() != Role.OWNER) {
           throw new ApiException(403, "Bạn không có quyền xử lý yêu cầu tham gia dự án này");
       }
       if(member.getStatus() == MemberStatus.REMOVED){
           throw new ApiException(403, "Bạn đã bị xóa khỏi dự án này");
       }
        JoinRequest joinRequest = joinRequestRepository.findByUserIdAndProjectId(userId, projectId);


        if (joinRequest == null || joinRequest.getStatus() != JoinStatus.PENDING) {
            throw new ApiException(400, "Yêu cầu tham gia không tồn tại hoặc đã được xử lý");
        }

        if (isApproved) {
            ProjectMember existingMember = projectMemberRepository.findMemberByProjectId(projectId, userId);
            if (existingMember != null) {
                if (existingMember.getStatus() == MemberStatus.ACTIVE) {
                    throw new ApiException(400, "Người dùng đã là thành viên của dự án");
                } else if (existingMember.getStatus() == MemberStatus.REMOVED) {
                    existingMember.setStatus(MemberStatus.ACTIVE);
                    projectMemberRepository.save(existingMember);
                }
            }else{
                ProjectMember projectMember = new ProjectMember();
                projectMember.setProject(joinRequest.getProject());
                projectMember.setUser(joinRequest.getUser());
                projectMember.setRole(Role.MEMBER);
                projectMember.setStatus(MemberStatus.ACTIVE);
                projectMemberRepository.save(projectMember);
            }

            notification.setTitle("Yêu cầu tham gia dự án được chấp nhận");
            notification.setUser(joinRequest.getUser());
            notification.setContent("Yêu cầu tham gia dự án " + joinRequest.getProject().getName() + " của bạn đã được chấp nhận");
            notification.setSender(currentUser);
            notification.setTargetId(joinRequest.getProject().getId());
            notification.setType(NotificationType.REQUEST_JOIN_APPROVED);
            notificationRepository.save(notification);
            joinRequest.setStatus(JoinStatus.APPROVED);
        } else {

            notification.setTitle("Yêu cầu tham gia dự án bị từ chối");
            notification.setUser(joinRequest.getUser());
            notification.setContent("Yêu cầu tham gia dự án " + joinRequest.getProject().getName() + " của bạn đã bị từ chối");
            notification.setSender(currentUser);
            notification.setTargetId(joinRequest.getProject().getId());
            notification.setType(NotificationType.REQUEST_JOIN_REJECTED);
            notificationRepository.save(notification);
            joinRequest.setStatus(JoinStatus.REJECTED);
        }
        joinRequestRepository.save(joinRequest);
        messaging.convertAndSend("/topic/notify/" + joinRequest.getUser().getId(),
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
        firebaseService.sendToToken(
                joinRequest.getUser().getTokenFcm(),
                notification.getTitle(),
                notification.getContent(),
                Map.of(
                        "id", String.valueOf(joinRequest.getProject().getId()),
                        "type", isApproved ? "REQUEST_JOIN_APPROVED" : "REQUEST_JOIN_REJECTED"
                )
        );
    }

    @Transactional
    @Override
    public void updateRoleMember(Integer projectId, Integer userId, int newRole) throws ApiException, FirebaseMessagingException {
        String email = securityUtil.getEmailRequest();
        User currentUser = userRepository.findByEmail(email);

        if(currentUser.getId() == userId){
            throw new ApiException(400, "Bạn không thể thay đổi vai trò của chính mình");
        }
        ProjectMember member = projectMemberRepository.findUserByProjectIdAndUserId(projectId, currentUser.getId());
        if (member == null || member.getRole() != Role.OWNER) {
            throw new ApiException(403, "Bạn không có quyền cập nhật vai trò thành viên dự án này");
        }
        if(member.getStatus() == MemberStatus.REMOVED){
            throw new ApiException(403, "Bạn đã bị xóa khỏi dự án này");
        }

        ProjectMember targetMember = projectMemberRepository.findUserByProjectIdAndUserId(projectId, userId);
        if (targetMember == null) {
            throw new ApiException(404, "Thành viên không tồn tại trong dự án");
        }

        if(targetMember.getStatus() == MemberStatus.REMOVED){
            throw new ApiException(403, "Thành viên đã bị xóa khỏi dự án này");
        }
        if (newRole != 0 && newRole != 1) {
            throw new ApiException(400, "Vai trò không hợp lệ");
        }

        Notification notification = new Notification();
        notification.setUser(targetMember.getUser());
        notification.setSender(currentUser);
        notification.setTargetId(projectId);
        notification.setType(NotificationType.PROJECT);
        if(newRole == 1){
            notification.setTitle("Cập nhật vai trò");
            notification.setContent(currentUser.getDisplayName() + " đã nâng cấp bạn lên vai trò Quản lý trong dự án");
        } else {
            notification.setTitle("Cập nhật vai trò");
            notification.setContent(currentUser.getDisplayName() + " đã hạ cấp bạn xuống vai trò Thành viên trong dự án");
        }
        notificationRepository.save(notification);



        targetMember.setRole(newRole == 0 ? Role.MEMBER : Role.OWNER);
        projectMemberRepository.save(targetMember);

        messaging.convertAndSend("/topic/notify/" + targetMember.getUser().getId(),
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

        firebaseService.sendToToken(
                targetMember.getUser().getTokenFcm(),
                notification.getTitle(),
                notification.getContent(),
                Map.of(
                        "id", String.valueOf(projectId),
                        "type", "PROJECT"
                )
        );
    }

    @Override
    public void deleteMember(Integer projectId, Integer userId) throws ApiException, FirebaseMessagingException {
        String email = securityUtil.getEmailRequest();
        User currentUser = userRepository.findByEmail(email);

        if(currentUser.getId() == userId){
            throw new ApiException(400, "Bạn không thể xóa chính mình khỏi dự án");
        }
        ProjectMember member = projectMemberRepository.findUserByProjectIdAndUserId(projectId, currentUser.getId());
        if (member == null || member.getRole() != Role.OWNER) {
            throw new ApiException(403, "Bạn không có quyền xóa thành viên khỏi dự án này");
        }
        if(member.getStatus() == MemberStatus.REMOVED){
            throw new ApiException(403, "Bạn đã bị xóa khỏi dự án này");
        }
        ProjectMember targetMember = projectMemberRepository.findUserByProjectIdAndUserId(projectId, userId);
        if (targetMember == null) {
            throw new ApiException(404, "Thành viên không tồn tại trong dự án");
        }
        if(targetMember.getStatus() == MemberStatus.REMOVED){
            throw new ApiException(403, "Thành viên đã bị xóa khỏi dự án này");
        }

        // Tìm tất cả các task trong project mà user đang được assign
        List<Task> assignedTasks = taskRepository.findTasksAssignedToUserInProject(userId, projectId);

        // Xóa user khỏi tất cả các task đó
        for (Task task : assignedTasks) {
            task.getAssignees().removeIf(assignee -> assignee.getId() == userId);
            taskRepository.save(task);
        }

        // Gửi thông báo chung về việc bị xóa khỏi dự án
        Notification notification = new Notification();
        notification.setTitle("Bạn đã bị xóa khỏi dự án");
        notification.setUser(targetMember.getUser());
        notification.setContent(currentUser.getDisplayName() + " đã xóa bạn khỏi dự án: "+ projectRepository.findById(projectId).get().getName());
        notification.setSender(currentUser);
        notification.setTargetId(projectId);
        notification.setType(NotificationType.PROJECT_REMOVE);
        notificationRepository.save(notification);

        // Xóa member khỏi project
        targetMember.setStatus(MemberStatus.REMOVED);
        projectMemberRepository.save(targetMember);

        messaging.convertAndSend("/topic/notify/" + targetMember.getUser().getId(),
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

        firebaseService.sendToToken(
                targetMember.getUser().getTokenFcm(),
                "Bạn đã bị xóa khỏi dự án",
                currentUser.getDisplayName() + " đã xóa bạn khỏi dự án",
                Map.of(
                        "id", String.valueOf(projectId),
                        "type", "PROJECT_REMOVE"
                )
        );
    }
    @Override
    public SearchResponseDto searchGlobally(String keyword) throws ApiException {
        String email = securityUtil.getEmailRequest();
        User user = userRepository.findByEmail(email);

        List<Project> projects = projectRepository.findAllPublicOrJoinedProjects(user.getId(), keyword);
        List<Task> tasks = taskRepository.searchAllTasksInProjectsUserJoined(user.getId(), keyword);

        List<ProjectResponseDto> projectsDto = projects.stream()
                .map(project -> {
                    // Lọc thành viên đang ACTIVE
                    List<UserResponse> activeMembers = project.getProjectMembers() == null
                            ? List.of()
                            : project.getProjectMembers().stream()
                            .filter(pm -> pm.getStatus() == MemberStatus.ACTIVE)
                            .map(pm -> {
                                User u = pm.getUser();
                                return UserResponse.builder()
                                        .displayName(u != null ? u.getDisplayName() : null)
                                        .email(u != null ? u.getEmail() : null)
                                        .avatar(u != null ? u.getAvatar() : null)
                                        .build();
                            })
                            .toList();

                    User owner = project.getCreatedBy();
                    int allTask = taskRepository.countAllTaskFromProject(project.getId());
                    int completedTask = taskRepository.countCompletedTaskFromProject(project.getId());
                    String progess = completedTask+"/"+allTask;

                    //check dealine with today
                    LocalDate today = LocalDate.now();
                    boolean isLate = project.getDeadline() != null && project.getDeadline().isBefore(today);

                    return ProjectResponseDto.builder()
                            .progress(progess)
                            .isLate(isLate)
                            .id(project.getId())
                            .name(project.getName())
                            .description(project.getDescription())
                            .deadline(project.getDeadline() != null ? project.getDeadline().toString() : null)
                            .createdBy(UserResponse.builder()
                                    .displayName(owner != null ? owner.getDisplayName() : null)
                                    .email(owner != null ? owner.getEmail() : null)
                                    .avatar(owner != null ? owner.getAvatar() : null)
                                    .build())
                            .members(activeMembers)
                            .build();
                })
                .toList();

        List<TaskResponseDto> tasksDto = tasks.stream()
                .map(task -> TaskResponseDto.builder()
                        .id(task.getId())
                        .title(task.getTitle())
                        .description(task.getDescription())
                        .dueDate(task.getDueDate() != null ? task.getDueDate().toString() : null)
                        .status(task.getStatus() != null ? task.getStatus().toString() : null)
                        .projectName(task.getProject() != null ? task.getProject().getName() : null)
                        .build())
                .toList();

        return SearchResponseDto.builder()
                .projects(projectsDto)
                .tasks(tasksDto)
                .build();
    }

    @Override
    public void UpdateProject(int id, UpdateProjectRequest updateProjectRequest) throws ApiException {
        String email = securityUtil.getEmailRequest();
        User currentUser = userRepository.findByEmail(email);
        Project project = projectRepository.findById(id)
                .orElseThrow(() -> new ApiException(404, "Dự án không tồn tại"));

        ProjectMember member = projectMemberRepository.findUserByProjectIdAndUserId(id, currentUser.getId());
        if (member == null || member.getRole() != Role.OWNER) {
            throw new ApiException(403, "Bạn không có quyền cập nhật dự án này");
        }
        if(member.getStatus() == MemberStatus.REMOVED){
            throw new ApiException(403, "Bạn đã bị xóa khỏi dự án này");
        }

        //compare deadline with today
        if (Util.parseToLocalDate(updateProjectRequest.getDeadline()).isBefore(Util.getCurrentLocalDate())) {
            throw new ApiException(400, "Deadline phải lớn hơn hoặc bằng ngày hiện tại");
        }

        //is Public is 0 or 1
        if (updateProjectRequest.getIsPublic() != 0 && updateProjectRequest.getIsPublic() != 1) {
            throw new ApiException(400, "Dữ liệu công khai phải là 0 hoặc 1");
        }

        project.setName(updateProjectRequest.getTitle());
        project.setDescription(updateProjectRequest.getDescription());
        project.setIsPublic(updateProjectRequest.getIsPublic());
        project.setDeadline(Util.parseToLocalDate(updateProjectRequest.getDeadline()));

        projectRepository.save(project);
    }

    @Transactional
    @Override
    public void deleteProject(int projectId) throws ApiException {
        String email = securityUtil.getEmailRequest();
        User currentUser = userRepository.findByEmail(email);
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ApiException(404, "Dự án không tồn tại"));
        ProjectMember member = projectMemberRepository.findUserByProjectIdAndUserId(projectId, currentUser.getId());
        if (member == null || member.getRole() != Role.OWNER) {
            throw new ApiException(403, "Bạn không có quyền xóa dự án này");
        }

        if(member.getStatus() == MemberStatus.REMOVED){
            throw new ApiException(403, "Bạn đã bị xóa khỏi dự án này");
        }

        projectRepository.delete(project);
    }

}
