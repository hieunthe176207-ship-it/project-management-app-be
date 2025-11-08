package com.fpt.project.dto.request;

import com.fpt.project.constant.TaskStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class TaskUpdateStatusRequest {
    @NotNull(message = "Task status is required")
    private TaskStatus status;
}