package com.fpt.project.dto.request;

import java.time.LocalDate;
import java.util.Set;
import lombok.Data;

public class TaskRequest {
    private String title;
    private String description;
    private LocalDate dueDate;
    private Long projectId;
    private Set<Long> assigneeIds;
}