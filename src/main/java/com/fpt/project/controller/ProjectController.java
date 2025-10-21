package com.fpt.project.controller;

import com.fpt.project.dto.ResponseSuccess;
import com.fpt.project.dto.request.ProjectCreateRequest;
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


    @GetMapping("/all")
    public  ResponseEntity<?> getAllProject() throws ApiException {
        List<Project> pr= projectService.findProjectById(0);
        return ResponseEntity.ok(ResponseSuccess.builder()
                        .code(201)
                        .message("Get all projects successfully")
                .build());

    }
}
