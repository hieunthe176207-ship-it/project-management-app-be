package com.fpt.project.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Set;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ProjectResponseDto {
    private Integer id;
    private String name;
    private String description;
    private String deadline;
    private UserResponse createdBy;
    private int isPublic;
    private int countJoinRequest;
    private List<UserResponse> members;
}
