package com.fpt.project.dto.response;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


@AllArgsConstructor
@Data
@NoArgsConstructor
@Builder
public class TaskResponseDto {
    private Integer id;
    private String title;
    private String description;
    private String dueDate;
    private String status;
    private String projectName;
}
