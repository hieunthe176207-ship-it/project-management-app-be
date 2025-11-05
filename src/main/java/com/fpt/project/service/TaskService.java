package com.fpt.project.service;

import com.fpt.project.dto.request.CreateTaskRequestDto;
import com.fpt.project.dto.response.TaskResponse;
import com.fpt.project.exception.ApiException;

import java.util.List;

public interface TaskService {
    List<TaskResponse> getTasksForCurrentUser();
    void addTaskToProject(CreateTaskRequestDto data) throws ApiException;
}
