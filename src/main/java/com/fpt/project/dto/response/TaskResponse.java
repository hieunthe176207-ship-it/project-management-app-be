package com.fpt.project.dto.response;

import com.fpt.project.constant.TaskStatus;
import com.fpt.project.entity.Task;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDate;
import java.util.Set;
import java.util.stream.Collectors;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TaskResponse {
    private Integer id;
    private String title;
    private String description;
    private String dueDate;
    private String status;

    private ProjectResponse project;
    private UserResponse createdBy;
    private Set<UserResponse> assignees;
}
