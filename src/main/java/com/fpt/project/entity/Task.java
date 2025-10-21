package com.fpt.project.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

@EqualsAndHashCode(callSuper = true)
@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Task extends BaseEntity {
    private String title;
    @Lob
    private String description;
    private LocalDate dueDate;


    @ManyToOne
    @JoinColumn(name="project_id", nullable=false)
    private Project project;

    @ManyToOne(optional=false)
    @JoinColumn(name="created_by")
    private User createdBy;

    @ManyToMany
    @JoinTable(
            name="task_assignees",
            joinColumns=@JoinColumn(name="task_id"),
            inverseJoinColumns=@JoinColumn(name="user_id")
    )
    private Set<User> assignees = new HashSet<>();
}
