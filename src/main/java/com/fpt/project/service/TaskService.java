package com.fpt.project.service;

import com.fpt.project.dto.request.CreateTaskRequestDto;
import com.fpt.project.dto.response.SubTaskResponse;
import com.fpt.project.dto.response.TaskDetailResponseDto;
import com.fpt.project.dto.response.TaskResponse;
import com.fpt.project.exception.ApiException;

import java.util.List;

public interface TaskService {
    List<TaskResponse> getTasksForCurrentUser();
    Integer addTaskToProject(CreateTaskRequestDto data) throws ApiException;
    List<TaskResponse> getAllTasksByProjectId(Integer projectId) throws ApiException;
    TaskDetailResponseDto getTaskDetailById(int id) throws ApiException;
    SubTaskResponse createSubTask(int taskId, String title) throws ApiException;
    void updateSubTaskComplete(int subTaskId, boolean completed) throws ApiException;
}
