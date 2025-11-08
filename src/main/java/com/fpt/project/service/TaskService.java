package com.fpt.project.service;

import com.fpt.project.dto.request.*;
import com.fpt.project.dto.response.*;
import com.fpt.project.exception.ApiException;
import com.google.firebase.messaging.FirebaseMessagingException;

import java.util.List;

public interface TaskService {
    KanbanBoardResponse getKanbanBoard(Integer projectId) throws ApiException;
    TaskResponse updateTaskStatus(Integer taskId, TaskUpdateStatusRequest request) throws ApiException;

    List<TaskResponseDto> getTasksForCurrentUser();
    Integer addTaskToProject(CreateTaskRequestDto data) throws ApiException, FirebaseMessagingException;
    List<ListTaskForProjectResponse> getAllTasksByProjectId(Integer projectId) throws ApiException;
    TaskDetailResponseDto getTaskDetailById(int id) throws ApiException;
    SubTaskResponse createSubTask(int taskId, String title) throws ApiException;
    void updateSubTaskComplete(int subTaskId, boolean completed) throws ApiException;
    Integer updateTask(UpdateTaskRequest data) throws ApiException, FirebaseMessagingException;
    void deleteTask(int taskId) throws ApiException, FirebaseMessagingException;
}
