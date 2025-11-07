package com.fpt.project.service.impl;

import com.fpt.project.constant.JoinStatus;
import com.fpt.project.constant.MemberStatus;
import com.fpt.project.constant.Role;
import com.fpt.project.dto.PageResponse;
import com.fpt.project.dto.request.ProjectCreateRequest;
import com.fpt.project.dto.response.ProjectResponseDto;
import com.fpt.project.dto.response.SearchResponseDto;
import com.fpt.project.dto.response.TaskResponseDto;
import com.fpt.project.dto.response.UserResponse;
import com.fpt.project.entity.*;
import com.fpt.project.exception.ApiException;
import com.fpt.project.repository.*;
import com.fpt.project.service.ProjectService;
import com.fpt.project.util.SecurityUtil;
import com.fpt.project.util.Util;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
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

    @Override
    public void addMembersToProject(Integer projectId, List<Integer> userIds) throws ApiException {
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
                    existingMember.setStatus(MemberStatus.ACTIVE);
                    projectMemberRepository.save(existingMember);
                }
            } else {
                ProjectMember newMember = new ProjectMember();
                newMember.setProject(project);
                newMember.setUser(user);
                newMember.setRole(Role.MEMBER);
                newMember.setStatus(MemberStatus.ACTIVE);
                projectMemberRepository.save(newMember);
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

        return projects.stream().map(project -> ProjectResponseDto.builder()
                .id(project.getId())
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
                .build()).toList();
    }

    @Override
    public void requestJoinPublicProject(Integer projectId) throws ApiException {
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
                   return;
               }
            }
        }

        JoinRequest joinRequest= new JoinRequest();
        joinRequest.setProject(project);
        joinRequest.setUser(user);
        joinRequest.setStatus(JoinStatus.PENDING);
        joinRequestRepository.save(joinRequest);
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
    public void handleJoinRequest(Integer projectId, Integer userId, boolean isApproved) throws ApiException {
       String email = securityUtil.getEmailRequest();
       User currentUser = userRepository.findByEmail(email);
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
            joinRequest.setStatus(JoinStatus.APPROVED);
        } else {
            joinRequest.setStatus(JoinStatus.REJECTED);
        }
        joinRequestRepository.save(joinRequest);
    }

    @Override
    public void updateRoleMember(Integer projectId, Integer userId, int newRole) throws ApiException {
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

        targetMember.setRole(newRole == 0 ? Role.MEMBER : Role.OWNER);
        projectMemberRepository.save(targetMember);
    }

    @Override
    public void deleteMember(Integer projectId, Integer userId) throws ApiException {
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

        targetMember.setStatus(MemberStatus.REMOVED);
        projectMemberRepository.save(targetMember);
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

                    return ProjectResponseDto.builder()
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

}
