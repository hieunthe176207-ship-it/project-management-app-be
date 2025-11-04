package com.fpt.project.controller;

import com.fpt.project.service.TaskService;
import com.fpt.project.entity.Task;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import java.util.List;

@RestController
@RequestMapping("/task")
public class TaskController {

    @Autowired
    private TaskService taskService;

    @GetMapping("/my-tasks")
    public ResponseEntity<List<Task>> getMyTasks() {
        List<Task> tasks = taskService.getTasksForCurrentUser();
        return ResponseEntity.ok(tasks);
    }

}
