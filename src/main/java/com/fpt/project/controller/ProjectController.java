package com.fpt.project.controller;

import com.fpt.project.dto.ResponseSuccess;
import com.fpt.project.dto.request.ProjectCreateRequest;
import com.fpt.project.dto.request.UpdateProjectRequest;
import com.fpt.project.dto.response.ProjectResponseDto;
import com.fpt.project.dto.response.SearchResponseDto;
import com.fpt.project.entity.Project;
import com.fpt.project.exception.ApiException;
import com.fpt.project.service.ProjectService;
import com.google.firebase.messaging.FirebaseMessagingException;
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
    public ResponseEntity<ResponseSuccess<Void>> addMembersToProject(@PathVariable Integer projectId, @RequestBody List<Integer> userIds) throws ApiException, FirebaseMessagingException {
        projectService.addMembersToProject(projectId, userIds);
        return ResponseEntity.ok(ResponseSuccess.<Void>builder()
                .code(200)
                .message("Members added successfully")
                .build());
    }

    @PatchMapping("/update/{id}")
    public ResponseEntity<?> updateMemberRole(@RequestParam("role") int role, @PathVariable int id, @RequestParam("userId") int userId) throws ApiException, FirebaseMessagingException {
        projectService.updateRoleMember(id, userId, role);
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

    @GetMapping("/public-projects")
    public ResponseEntity<?> getAllPublicProjects() throws ApiException {
        List<ProjectResponseDto> projects = projectService.getAllPublicProjects();
        return ResponseEntity.ok(ResponseSuccess.<List<ProjectResponseDto>>builder()
                .code(200)
                .message("Public projects retrieved successfully")
                .data(projects)
                .build());
    }

    @PostMapping("/join-request")
    public ResponseEntity<ResponseSuccess<Void>> joinPublicProject(@RequestParam Integer projectId) throws ApiException, FirebaseMessagingException {
        projectService.requestJoinPublicProject(projectId);
        return ResponseEntity.ok(ResponseSuccess.<Void>builder()
                .code(200)
                .message("Joined public project successfully")
                .build());
    }

    @GetMapping("/get-join-requests/{projectId}")
    public ResponseEntity<?> getJoinRequests(@PathVariable Integer projectId) throws ApiException {
        return ResponseEntity.ok(ResponseSuccess.<List<?>>builder()
                .code(200)
                .data(projectService.getPendingJoinRequests(projectId))
                .message("Join requests retrieved successfully")
                .build());
    }

    @PostMapping("/handle-join-request")
    public ResponseEntity<ResponseSuccess<Void>> handleJoinRequest(
            @RequestParam Integer projectId,
            @RequestParam Integer userId,
            @RequestParam boolean isApproved) throws ApiException, FirebaseMessagingException {
        projectService.handleJoinRequest(projectId, userId, isApproved);
        return ResponseEntity.ok(ResponseSuccess.<Void>builder()
                .code(200)
                .message("Join request handled successfully")
                .build());
    }

    @DeleteMapping("/remove-member/{projectId}/{userId}")
    public ResponseEntity<ResponseSuccess<Void>> removeMember(
            @PathVariable Integer projectId,
            @PathVariable Integer userId) throws ApiException, FirebaseMessagingException {
        projectService.deleteMember(projectId, userId);
        return ResponseEntity.ok(ResponseSuccess.<Void>builder()
                .code(200)
                .message("Member removed successfully")
                .build());
    }

    @GetMapping("/search-global")
    public ResponseEntity<?> searchProjectsGlobally(@RequestParam String keyword) throws ApiException {
        SearchResponseDto data = projectService.searchGlobally(keyword);
        return ResponseEntity.ok(ResponseSuccess.<SearchResponseDto>builder()
                .code(200)
                .message("Projects retrieved successfully")
                .data(data)
                .build());
    }

    @PutMapping("/update-project/{projectId}")
    public ResponseEntity<ResponseSuccess<Void>> updateProject(
            @PathVariable Integer projectId,
            @RequestBody UpdateProjectRequest updateProjectRequest) throws ApiException {
        // Logic to update a project
        projectService.UpdateProject(projectId, updateProjectRequest);
        return ResponseEntity.ok(ResponseSuccess.<Void>builder()
                .code(200)
                .message("Project updated successfully")
                .build());
    }

}
