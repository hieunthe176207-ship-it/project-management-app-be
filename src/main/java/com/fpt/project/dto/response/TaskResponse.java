package com.fpt.project.dto.response;

import com.fpt.project.constant.TaskStatus;
import com.fpt.project.entity.Task;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDate;
import java.util.Set;
import java.util.stream.Collectors;

@Data
@NoArgsConstructor
public class TaskResponse {
    private Integer id;
    private String title;
    private String description;
    private LocalDate dueDate;
    private TaskStatus status;

    private ProjectResponse project;
    private UserResponse createdBy;
    private Set<UserResponse> assignees;

    public TaskResponse(Task task) {
        this.id = task.getId();
        this.title = task.getTitle();
        this.description = task.getDescription();
        this.dueDate = task.getDueDate();
        this.status = task.getStatus();

        if (task.getProject() != null) {
            this.project = new ProjectResponse(task.getProject());
        }
        if (task.getCreatedBy() != null) {
            this.createdBy = new UserResponse(task.getCreatedBy());
        }
        if (task.getAssignees() != null) {
            this.assignees = task.getAssignees().stream()
                    .map(UserResponse::new)
                    .collect(Collectors.toSet());
        }
    }
}
