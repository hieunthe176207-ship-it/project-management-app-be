package com.fpt.project.service.impl;

import com.fpt.project.dto.PageResponse;
import com.fpt.project.dto.request.ProjectCreateRequest;
import com.fpt.project.dto.response.ProjectResponse;
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

import java.util.List;

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

        Project project = Project.builder()
                .name(projectCreateRequest.getName())
                .description(projectCreateRequest.getDescription())
                .createdBy(user)
                .deadline(Util.parseToLocalDate(projectCreateRequest.getDeadline()))
                .build();
        projectRepository.save(project);
    }

    @Override
    public PageResponse<ProjectResponse> findAllProjectsByUserId( int page, int size) {
        return null;
    }

    @Override
    public List<Project> findProjectById(int id) throws ApiException {
        List<Project> prs = projectRepository.findAll();
        return prs;
    }
}
