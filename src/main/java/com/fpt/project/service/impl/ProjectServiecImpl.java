package com.fpt.project.service.impl;

import com.fpt.project.constant.JoinStatus;
import com.fpt.project.constant.Role;
import com.fpt.project.dto.PageResponse;
import com.fpt.project.dto.request.ProjectCreateRequest;
import com.fpt.project.dto.response.ProjectResponseDto;
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


    @Override
    public List<ProjectResponseDto> findProjectByUser() throws ApiException {
        String email = securityUtil.getEmailRequest();
        List<Project> projects = projectRepository.findAllByUserEmail(email);
        List<ProjectResponseDto> projectResponseDtos = projects.stream().map(project -> {

            List<ProjectMember> pm = project.getProjectMembers();
            pm.stream().forEach(p -> {
                System.out.println("Member: " + p.getUser().getDisplayName());
            });

            return ProjectResponseDto.builder()
                    .id(project.getId())
                    .name(project.getName())
                    .description(project.getDescription())
                    .deadline(project.getDeadline().toString())
                    .createdBy(UserResponse.builder()
                            .displayName(project.getCreatedBy().getDisplayName())
                            .email(project.getCreatedBy().getEmail())
                            .avatar(project.getCreatedBy().getAvatar())
                            .build())
                    .members(pm.stream().map(p -> UserResponse.builder()
                            .displayName(p.getUser().getDisplayName())
                            .email(p.getUser().getEmail())
                            .avatar(p.getUser().getAvatar())
                            .build()).toList())
                    .build();
        }).toList();
        return projectResponseDtos;
    }

    @Override
    public ProjectResponseDto getProjectById(Integer projectId) throws ApiException {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ApiException(404, "Project not found"));
        int countJoinRequest = joinRequestRepository.countRecordsPendingByProjectId(projectId);
        User owner = project.getCreatedBy();
        List<ProjectMember> pm = project.getProjectMembers();
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
                .members(pm.stream().map(p -> UserResponse.builder()
                        .displayName(p.getUser().getDisplayName())
                        .email(p.getUser().getEmail())
                        .avatar(p.getUser().getAvatar())
                        .build()).toList())
                .build();
    }

    @Override
    public void addMembersToProject(Integer projectId, List<Integer> userIds) throws ApiException {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ApiException( 400, "Project not found"));
        for (Integer userId : userIds) {
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new ApiException(404, "User not found: " + userId));
            boolean exists = projectMemberRepository.existsByProjectIdAndUserId(projectId, userId);
            if (!exists) {
                ProjectMember member = new ProjectMember();
                member.setProject(project);
                member.setUser(user);
                member.setRole(Role.MEMBER);
                projectMemberRepository.save(member);
            }
        }
    }


    @Override
    public List<UserResponse> getUsersByProjectId(Integer projectId) throws ApiException {
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
            } else if (existingRequest.getStatus() == JoinStatus.APPROVED) {
                throw new ApiException(400, "Bạn đã là thành viên của dự án này");
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
        JoinRequest joinRequest = joinRequestRepository.findByUserIdAndProjectId(userId, projectId);


        if (joinRequest == null || joinRequest.getStatus() != JoinStatus.PENDING) {
            throw new ApiException(400, "Yêu cầu tham gia không tồn tại hoặc đã được xử lý");
        }

        if (isApproved) {
            joinRequest.setStatus(JoinStatus.APPROVED);
            // Add user to project members
            ProjectMember projectMember = new ProjectMember();
            projectMember.setProject(joinRequest.getProject());
            projectMember.setUser(joinRequest.getUser());
            projectMember.setRole(Role.MEMBER);
            projectMemberRepository.save(projectMember);
        } else {
            joinRequest.setStatus(JoinStatus.REJECTED);
        }
        joinRequestRepository.save(joinRequest);
    }
}
