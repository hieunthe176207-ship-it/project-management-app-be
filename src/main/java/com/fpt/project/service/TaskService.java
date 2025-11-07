package com.fpt.project.service;

import com.fpt.project.dto.request.TaskCreateRequest;
import com.fpt.project.dto.request.TaskUpdateAssigneesRequest;
import com.fpt.project.dto.request.TaskUpdateStatusRequest;
import com.fpt.project.dto.response.KanbanBoardResponse;
import com.fpt.project.dto.request.CreateTaskRequestDto;
import com.fpt.project.dto.response.TaskResponse;
import com.fpt.project.exception.ApiException;

import java.util.List;

public interface TaskService {
    KanbanBoardResponse getKanbanBoard(Integer projectId) throws ApiException;
    TaskResponse updateTaskStatus(Integer taskId, TaskUpdateStatusRequest request) throws ApiException;

    List<TaskResponse> getTasksForCurrentUser();
    void addTaskToProject(CreateTaskRequestDto data) throws ApiException;
}