package com.fpt.project.service.impl;

import com.fpt.project.dto.PageResponse;
import com.fpt.project.dto.request.ProjectCreateRequest;
import com.fpt.project.dto.response.ProjectResponseDto;
import com.fpt.project.dto.response.UserResponse;
import com.fpt.project.entity.Project;
import com.fpt.project.entity.User;
import com.fpt.project.exception.ApiException;
import com.fpt.project.repository.ProjectRepository;
import com.fpt.project.repository.UserRepository;
import com.fpt.project.service.ProjectService;
import com.fpt.project.util.SecurityUtil;
import com.fpt.project.util.Util;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class ProjectServiecImpl implements ProjectService {

    private final ProjectRepository projectRepository;
    private final SecurityUtil securityUtil;
    private final UserRepository userRepository;

    @Override
    public void saveProject(ProjectCreateRequest projectCreateRequest) throws ApiException {
        String email = securityUtil.getEmailRequest();

        User user = userRepository.findByEmail(email);

        if(user == null) {
            throw new ApiException(400, "Tài khoản không tồn tại");
        }
        Set<User> members = new HashSet<>();
        members.add(user);

        Project project = Project.builder()
                .name(projectCreateRequest.getName())
                .description(projectCreateRequest.getDescription())
                .createdBy(user)
                .deadline(Util.parseToLocalDate(projectCreateRequest.getDeadline()))
                .members(members)
                .build();
        projectRepository.save(project);
    }

    @Override
    public List<ProjectResponseDto> findProjectByUser() throws ApiException {
        String email = securityUtil.getEmailRequest();
        List<Project> projects = projectRepository.findAllByUserEmail(email);
        List<ProjectResponseDto> projectResponseDtos = projects.stream().map(project -> {
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
                    .members(project.getMembers().stream().map(member -> UserResponse.builder()
                            .displayName(member.getDisplayName())
                            .email(member.getEmail())
                            .avatar(member.getAvatar())
                            .build()).collect(java.util.stream.Collectors.toSet()))
                    .build();
        }).toList();
        return projectResponseDtos;
    }

    @Override
    public ProjectResponseDto getProjectById(Integer projectId) throws ApiException {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ApiException(404, "Project not found"));

        User owner = project.getCreatedBy();
        Set<User> members = project.getMembers();

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
                .members(members.stream().map(member -> UserResponse.builder()
                        .displayName(member.getDisplayName())
                        .email(member.getEmail())
                        .avatar(member.getAvatar())
                        .build()).collect(java.util.stream.Collectors.toSet()))
                .build();
    }

}
