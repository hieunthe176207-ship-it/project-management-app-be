package com.fpt.project.service;

import com.fpt.project.dto.response.TaskResponse;
import java.util.List;

public interface TaskService {
    List<TaskResponse> getTasksForCurrentUser();

}
