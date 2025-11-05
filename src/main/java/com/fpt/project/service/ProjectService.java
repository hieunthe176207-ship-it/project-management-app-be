package com.fpt.project.service;

import com.fpt.project.dto.request.ProjectCreateRequest;
import com.fpt.project.dto.response.ProjectResponseDto;
import com.fpt.project.dto.response.UserResponse;
import com.fpt.project.entity.Project;
import com.fpt.project.exception.ApiException;

import java.util.List;

public interface ProjectService {
    void saveProject(ProjectCreateRequest projectCreateRequest) throws ApiException;
    List<ProjectResponseDto> findProjectByUser() throws ApiException;
    ProjectResponseDto getProjectById(Integer projectId) throws ApiException;
    void addMembersToProject(Integer projectId, List<Integer> userIds) throws ApiException;

    List<UserResponse> getUsersByProjectId(Integer projectId) throws ApiException;
}
