package com.fpt.project.entity;

import com.fpt.project.constant.ProjectStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.util.List;

@EqualsAndHashCode(callSuper = true)
@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Project extends BaseEntity {
    private String name;


    @ManyToOne(optional=false)
    @JoinColumn(name="created_by")
    private User createdBy;
    private LocalDate deadline;
    private String description;

    @OneToMany(mappedBy = "project", fetch = FetchType.LAZY)
    private List<Task> tasks;


}
