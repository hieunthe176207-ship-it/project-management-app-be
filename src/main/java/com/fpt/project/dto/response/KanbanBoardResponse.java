
package com.fpt.project.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class KanbanBoardResponse {
    private Integer projectId;
    private String projectName;
    private List<TaskResponse> todoTasks;
    private List<TaskResponse> inProgressTasks;
    private List<TaskResponse> inReviewTasks;
    private List<TaskResponse> doneTasks;
}