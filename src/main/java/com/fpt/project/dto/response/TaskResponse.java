package com.fpt.project.dto.response;

import com.fpt.project.constant.TaskStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TaskResponse {
    private Long id;
    private String title;
    private String description;
    private String dueDate;
    private TaskStatus status;
    private Long projectId;
    private UserResponse createdBy;
    private List<UserResponse> assignees;
    private String createdAt;
    private String updatedAt;


}