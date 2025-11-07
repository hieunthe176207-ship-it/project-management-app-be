
package com.fpt.project.controller;

import com.fpt.project.dto.request.TaskCreateRequest;
import com.fpt.project.dto.request.TaskUpdateAssigneesRequest;
import com.fpt.project.dto.request.TaskUpdateStatusRequest;
import com.fpt.project.dto.response.KanbanBoardResponse;
import com.fpt.project.dto.response.ResponseSuccess;
import com.fpt.project.dto.response.TaskResponse;
import com.fpt.project.entity.Task;
import com.fpt.project.exception.ApiException;
import com.fpt.project.service.TaskService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/tasks")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class TaskController {

    private final TaskService taskService;

    @GetMapping("/kanban/{projectId}")

    public ResponseEntity<ResponseSuccess<KanbanBoardResponse>> getKanbanBoard(@PathVariable Integer projectId) throws ApiException {
        KanbanBoardResponse kanbanBoard = taskService.getKanbanBoard(projectId);

        return ResponseEntity.ok(ResponseSuccess.<KanbanBoardResponse>builder()
                .status(HttpStatus.OK.value())
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
                .status(HttpStatus.OK.value())
                .message("Task status updated successfully")
                .data(taskResponse)
                .build());
    }



}