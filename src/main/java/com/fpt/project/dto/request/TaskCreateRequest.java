package com.fpt.project.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;

@Data
public class TaskCreateRequest {
    @NotBlank(message = "Task title is required")
    private String title;

    private String description;

    private LocalDate dueDate;

    @NotNull(message = "Project ID is required")
    private Integer projectId;

    private List<Long> assigneeIds;
}