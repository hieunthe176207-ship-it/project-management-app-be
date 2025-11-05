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

    @GetMapping("/member/{projectId}")
    public ResponseEntity<?> getUsersByProjectId(@PathVariable Integer projectId) throws ApiException {
        List<?> users = projectService.getUsersByProjectId(projectId);
        return ResponseEntity.ok(ResponseSuccess.<List<?>>builder()
                .code(200)
                .message("Users retrieved successfully")
                .data(users)
                .build());
    }

    @PostMapping("/add-members/{projectId}")
    public ResponseEntity<ResponseSuccess<Void>> addMembersToProject(@PathVariable Integer projectId, @RequestBody List<Integer> userIds) throws ApiException {
        projectService.addMembersToProject(projectId, userIds);
        return ResponseEntity.ok(ResponseSuccess.<Void>builder()
                .code(200)
                .message("Members added successfully")
                .build());
    }

    @PatchMapping("/update/{id}")
    public ResponseEntity<?> updateMemberRole(@RequestParam("role") int role){
        return ResponseEntity.ok(ResponseSuccess.<Void>builder()
                .code(200)
                .message("Update member success")
                .build());
    }

    @DeleteMapping("/delete-member/{id}")
    public ResponseEntity<?> deleteMember(@RequestParam("role") int role, @PathVariable int id){
        return ResponseEntity.ok(ResponseSuccess.<Void>builder()
                .code(200)
                .message("Update member success")
                .build());
    }

    @DeleteMapping("/delete-project/{id}")
    public ResponseEntity<?> deleteProject(@PathVariable int id){
        return ResponseEntity.ok(ResponseSuccess.<Void>builder()
                .code(200)
                .message("Update member success")
                .build());
    }





}
