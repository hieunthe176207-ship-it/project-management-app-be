package com.fpt.project.controller;

import com.fpt.project.dto.ResponseSuccess;
import com.fpt.project.dto.request.ProjectCreateRequest;
import com.fpt.project.dto.response.ProjectResponseDto;
import com.fpt.project.entity.Project;
import com.fpt.project.exception.ApiException;
import com.fpt.project.service.ProjectService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/project")
@RequiredArgsConstructor
public class ProjectController {

    private final ProjectService projectService;

    @PostMapping("/create")
    public ResponseEntity<ResponseSuccess<Void>> createProject(@RequestBody ProjectCreateRequest projectCreateRequest) throws ApiException {
        // Logic to create a project
        projectService.saveProject(projectCreateRequest);
        return ResponseEntity.ok(ResponseSuccess.<Void>builder()
                .code(200)
                .message("Project created successfully")
                .build());

    }
    @GetMapping("/my-projects")
    public ResponseEntity<?> getMyProjects() throws ApiException {
        List<ProjectResponseDto> projects = projectService.findProjectByUser();
        return ResponseEntity.ok(ResponseSuccess.<List<ProjectResponseDto>>builder()
                .code(200)
                .message("Projects retrieved successfully")
                .data(projects)
                .build());
    }

    @GetMapping("/get/{id}")
    public ResponseEntity<?> getProjectById(@PathVariable Integer id) throws ApiException {
        ProjectResponseDto project = projectService.getProjectById(id);
        return ResponseEntity.ok(ResponseSuccess.<ProjectResponseDto>builder()
                .code(200)
                .message("Project retrieved successfully")
                .data(project)
                .build());
    }
}
