package com.fpt.project.entity;

import com.fpt.project.constant.Role;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

@EqualsAndHashCode(callSuper = true)
@Entity
@AllArgsConstructor
@NoArgsConstructor
@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ProjectMember extends BaseEntity {
    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    User user;

    @ManyToOne
    @JoinColumn(name = "project_id", nullable = false)
    Project project;

    // Có thể thêm các trường khác như role, joinedDate, etc.
    @Enumerated(EnumType.STRING)
    Role role;

    private int lastReadMessageId = 0;
}
