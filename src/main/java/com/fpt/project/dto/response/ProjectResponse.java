package com.fpt.project.dto.response;

import com.fpt.project.entity.Project;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class ProjectResponse {
    private Integer id;
    private String name;

    public ProjectResponse(Project project) {
        this.id = project.getId();
        this.name = project.getName();
    }
}
