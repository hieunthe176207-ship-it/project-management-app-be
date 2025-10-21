package com.fpt.project.service;

import com.fpt.project.dto.PageResponse;
import com.fpt.project.dto.request.ProjectCreateRequest;
import com.fpt.project.dto.response.ProjectResponse;
import com.fpt.project.entity.Project;
import com.fpt.project.exception.ApiException;

import java.util.List;

public interface ProjectService {
    void saveProject(ProjectCreateRequest projectCreateRequest) throws ApiException;
    PageResponse<ProjectResponse> findAllProjectsByUserId(int page, int size);
    List<Project> findProjectById(int id) throws ApiException;
}
