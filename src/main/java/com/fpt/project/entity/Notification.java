package com.fpt.project.entity;

import com.fpt.project.constant.NotificationType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Notification extends BaseEntity{
    private String title;
    private String content;
    private Boolean isRead = false;
    @Enumerated(EnumType.STRING)
    private NotificationType type;
    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne
    @JoinColumn(name = "sender_id")
    private User sender;

}
