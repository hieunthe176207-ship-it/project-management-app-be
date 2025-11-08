package com.fpt.project.dto.response;

import com.fpt.project.constant.TaskStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.Set;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class ListTaskForProjectResponse {
    private Integer id;
    private String title;
    private String description;
    private String dueDate;
    private TaskStatus status;

    private ProjectResponse project;
    private UserResponse createdBy;
    private Set<UserResponse> assignees;
}
