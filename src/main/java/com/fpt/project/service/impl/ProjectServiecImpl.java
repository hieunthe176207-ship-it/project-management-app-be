package com.fpt.project.service.impl;

import com.fpt.project.constant.Role;
import com.fpt.project.dto.PageResponse;
import com.fpt.project.dto.request.ProjectCreateRequest;
import com.fpt.project.dto.response.ProjectResponseDto;
import com.fpt.project.dto.response.UserResponse;
import com.fpt.project.entity.ChatGroup;
import com.fpt.project.entity.Project;
import com.fpt.project.entity.ProjectMember;
import com.fpt.project.entity.User;
import com.fpt.project.exception.ApiException;
import com.fpt.project.repository.ChatGroupRepository;
import com.fpt.project.repository.ProjectMemberRepository;
import com.fpt.project.repository.ProjectRepository;
import com.fpt.project.repository.UserRepository;
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

    @Override
    @Transactional
    public void saveProject(ProjectCreateRequest projectCreateRequest) throws ApiException {
        String email = securityUtil.getEmailRequest();
        User user = userRepository.findByEmail(email);
        if (user == null) {
            throw new ApiException(400, "Tài khoản không tồn tại");
        }

        Project project = Project.builder()
                .name(projectCreateRequest.getName())
                .description(projectCreateRequest.getDescription())
                .createdBy(user)
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

        User owner = project.getCreatedBy();
        List<ProjectMember> pm = project.getProjectMembers();
        return ProjectResponseDto.builder()
                .id(project.getId())
                .name(project.getName())
                .description(project.getDescription())
                .deadline(project.getDeadline().toString())
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

}
