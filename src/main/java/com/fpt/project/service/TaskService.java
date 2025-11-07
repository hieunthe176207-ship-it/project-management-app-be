package com.fpt.project.service;

import com.fpt.project.dto.request.TaskCreateRequest;
import com.fpt.project.dto.request.TaskUpdateAssigneesRequest;
import com.fpt.project.dto.request.TaskUpdateStatusRequest;
import com.fpt.project.dto.response.KanbanBoardResponse;
import com.fpt.project.dto.response.TaskResponse;
import com.fpt.project.exception.ApiException;

public interface TaskService {
    KanbanBoardResponse getKanbanBoard(Integer projectId) throws ApiException;
    TaskResponse updateTaskStatus(Integer taskId, TaskUpdateStatusRequest request) throws ApiException;

}