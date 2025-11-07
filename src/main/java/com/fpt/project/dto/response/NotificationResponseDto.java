package com.fpt.project.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class NotificationResponseDto {
    Integer id;
    String title;
    String content;
    Boolean isRead;
    String type;
    String createdAt;
    UserResponse user;
    UserResponse sender;
}
