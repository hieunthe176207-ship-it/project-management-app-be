package com.fpt.project.service;


import com.fpt.project.entity.Task;
import java.util.List;

public interface TaskService {
    List<Task> getTasksForCurrentUser();

}
