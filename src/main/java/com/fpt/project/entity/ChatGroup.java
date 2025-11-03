package com.fpt.project.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.List;

@Entity
@Table(name = "chat_groups")
@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class ChatGroup extends BaseEntity {

    private String name; // "/topic/project.{id}" — để frontend subscribe
    private String avatar;
    // Mối quan hệ 1–1 với Project
    @OneToOne
    @JoinColumn(name = "project_id")
    private Project project;

    @OneToMany(mappedBy = "group", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Message> messages;
}
