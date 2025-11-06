package com.fpt.project.controller;

import com.fpt.project.dto.ResponseSuccess;
import com.fpt.project.dto.request.CreateTaskRequestDto;
import com.fpt.project.exception.ApiException;
import com.fpt.project.service.TaskService;
import com.fpt.project.dto.response.TaskResponse;

import org.springframework.web.bind.annotation.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;

import java.util.List;

@RestController
@RequestMapping("/task")
public class TaskController {

    @Autowired
    private TaskService taskService;

    @GetMapping("/my-tasks")
    public ResponseEntity<List<TaskResponse>> getMyTasks() {
        List<TaskResponse> tasks = taskService.getTasksForCurrentUser();
        return ResponseEntity.ok(tasks);
    }

    @PostMapping("/create")
    public ResponseEntity<String> createTask(@RequestBody CreateTaskRequestDto createTaskRequestDto) throws ApiException {
        // Logic to create a task
        taskService.addTaskToProject(createTaskRequestDto);
        return ResponseEntity.ok(ResponseSuccess.<String>builder()
                .code(200)
                .message("Task created successfully")
                .build().toString());
    }



}
