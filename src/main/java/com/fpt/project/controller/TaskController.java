package com.fpt.project.controller;

import com.fpt.project.dto.ResponseSuccess;
import com.fpt.project.dto.request.CreateTaskRequestDto;
import com.fpt.project.dto.request.TaskUpdateStatusRequest;
import com.fpt.project.dto.response.KanbanBoardResponse;
import com.fpt.project.exception.ApiException;
import com.fpt.project.service.TaskService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;

import com.fpt.project.dto.response.TaskResponse;

import org.springframework.web.bind.annotation.*;


import java.util.List;

@RestController
@RequestMapping("/task")
@RequiredArgsConstructor
public class TaskController {

    private final TaskService taskService;

    @GetMapping("/kanban/{projectId}")

    public ResponseEntity<ResponseSuccess<KanbanBoardResponse>> getKanbanBoard(@PathVariable Integer projectId) throws ApiException {
        KanbanBoardResponse kanbanBoard = taskService.getKanbanBoard(projectId);

        return ResponseEntity.ok(ResponseSuccess.<KanbanBoardResponse>builder()
                .code(200)
                .message("Nhận bảng kanban thành công")
                .data(kanbanBoard)
                .build());
    }

    @PutMapping("/{taskId}/status")
    public ResponseEntity<ResponseSuccess<TaskResponse>> updateTaskStatus(
            @PathVariable Integer taskId,
            @Valid @RequestBody TaskUpdateStatusRequest request) throws ApiException {

        TaskResponse taskResponse = taskService.updateTaskStatus(taskId, request);

        return ResponseEntity.ok(ResponseSuccess.<TaskResponse>builder()
                .code(200)
                .message("Task status updated successfully")
                .data(taskResponse)
                .build());
    }

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
