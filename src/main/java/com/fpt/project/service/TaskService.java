package com.fpt.project.service;


import com.fpt.project.entity.Task;
import com.fpt.project.dto.request.TaskRequest;
import java.util.List;

public interface TaskService {
    List<Task> getTasksForCurrentUser();
    Task createTask(TaskRequest taskRequest);
}
