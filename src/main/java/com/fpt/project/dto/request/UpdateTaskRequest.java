package com.fpt.project.dto.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UpdateTaskRequest {
    private Integer id;
    private String title;
    private String description;
    private String dueDate;
    private List<Integer> assigneeIds;

}
