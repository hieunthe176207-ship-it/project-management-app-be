package com.fpt.project.dto.response;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class ProjectResponse {
    private int id;
    private String name;
    private String description;
    private String deadline;
    private String status;
    private String createdBy;
}
