package com.fpt.project.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.Check;

import java.util.List;

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
}
