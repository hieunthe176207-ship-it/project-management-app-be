package com.fpt.project.entity;

import com.fpt.project.constant.ProjectStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@EqualsAndHashCode(callSuper = true)
@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Project extends BaseEntity {
    private String name;
    private int isPublic ; // 0: private, 1: public
    @ManyToOne(optional=false)
    @JoinColumn(name="created_by")
    private User createdBy;
    private LocalDate deadline;
    private String description;
    @OneToMany(mappedBy = "project", fetch = FetchType.LAZY)
    private List<Task> tasks;
    @OneToMany(mappedBy = "project", cascade = CascadeType.ALL, orphanRemoval = true)
    List<ProjectMember> projectMembers = new ArrayList<>();

    @OneToMany(mappedBy = "project", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<JoinRequest> joinRequests = new ArrayList<>();

}
