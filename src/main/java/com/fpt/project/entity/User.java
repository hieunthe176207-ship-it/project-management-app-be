package com.fpt.project.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.Check;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@FieldDefaults(level = AccessLevel.PRIVATE)
public class User extends BaseEntity {
    @Column(nullable=false, unique=true, length=120)
    private String email;
    @Column(name="password", nullable=false)
    private String password;
    @Column(name="display_name", nullable=false, length=80)
    private String displayName;
    private String avatar;

    @ManyToMany(mappedBy = "members", fetch = FetchType.LAZY)
    private Set<Project> joinedProjects = new HashSet<>();
}
