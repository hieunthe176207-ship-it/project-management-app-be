package com.fpt.project.controller;

import com.fpt.project.dto.ResponseSuccess;
import com.fpt.project.dto.request.CreateTaskRequestDto;
import com.fpt.project.dto.request.SubTaskRequestDto;
import com.fpt.project.dto.request.TaskUpdateStatusRequest;
import com.fpt.project.dto.request.UpdateTaskRequest;
import com.fpt.project.dto.response.*;
import com.fpt.project.exception.ApiException;
import com.fpt.project.service.TaskService;

import com.google.firebase.messaging.FirebaseMessagingException;
import jakarta.validation.Valid;
import org.springframework.data.jpa.repository.Query;
import org.springframework.web.bind.annotation.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;

import java.util.List;

@RestController
@RequestMapping("/task")
public class TaskController {

    @Autowired
    private TaskService taskService;

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
    public ResponseEntity<?> getMyTasks() {
        List<TaskResponseDto> tasks = taskService.getTasksForCurrentUser();
        return ResponseEntity.ok(ResponseSuccess.<List<TaskResponseDto>>builder()
                .code(200)
                .message("Tasks retrieved successfully")
                .data(tasks)
                .build());
    }

    @PostMapping("/create")
    public ResponseEntity<?> createTask(@RequestBody CreateTaskRequestDto createTaskRequestDto) throws ApiException, FirebaseMessagingException {
        // Logic to create a task

        return ResponseEntity.ok(ResponseSuccess.builder()
                .code(200)
                .data(taskService.addTaskToProject(createTaskRequestDto))
                .message("Task created successfully")
                .build());
    }

    @GetMapping("/all-task-for-project/{projectId}")
    public ResponseEntity<?> getAllTasksByProjectId(@PathVariable Integer projectId) throws ApiException {
        List<ListTaskForProjectResponse> data = taskService.getAllTasksByProjectId(projectId);
        return ResponseEntity.ok(ResponseSuccess.builder()
                .code(200)
                .message("Get all tasks for project successfully")
                .data(data)
                .build());
    }

    @GetMapping("/detail/{id}")
    public ResponseEntity<?> getTaskDetail(@PathVariable int id) throws ApiException {
        // Logic to get task detail
        // Assuming taskService has a method getTaskDetailById
        TaskDetailResponseDto taskDetail = taskService.getTaskDetailById(id);
        return ResponseEntity.ok(ResponseSuccess.<TaskDetailResponseDto>builder()
                .code(200)
                .message("Get task detail successfully")
                .data(taskDetail)
                .build());
    }
    @PostMapping("/add-subtask/{id}")
    public ResponseEntity<?> addSubTask(@PathVariable int id, @RequestBody SubTaskRequestDto data) throws ApiException {
        SubTaskResponse subTask = taskService.createSubTask(id, data.getName());
        return ResponseEntity.ok(ResponseSuccess.<SubTaskResponse>builder()
                .code(200)
                .message("Add subtask successfully")
                .data(subTask)
                .build());
    }

    @PatchMapping("/update-subtask-completed/{id}")
    public ResponseEntity<?> updateSubTask(@PathVariable int id, @RequestParam boolean completed) throws ApiException {
        taskService.updateSubTaskComplete(id, completed);
        // Logic to update subtask
        return ResponseEntity.ok(ResponseSuccess.<Void>builder()
                .code(200)
                .message("Update subtask successfully")
                .build());
    }
    @PutMapping("/update-task")
    public ResponseEntity<?> updateTask(@RequestBody UpdateTaskRequest data) throws ApiException, FirebaseMessagingException {
        return ResponseEntity.ok(ResponseSuccess.builder()
                .code(200)
                .data(taskService.updateTask(data))
                .message("Update task status successfully")
                .build());
    }

    @DeleteMapping("/delete-task/{taskId}")
    public ResponseEntity<?> deleteTask(@PathVariable int taskId) throws ApiException, FirebaseMessagingException {
        taskService.deleteTask(taskId);
        return ResponseEntity.ok(ResponseSuccess.<Void>builder()
                .code(200)
                .message("Delete task successfully")
                .build());
        }


}
