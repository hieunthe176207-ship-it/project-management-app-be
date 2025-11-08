package com.fpt.project.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
@FieldDefaults(level = lombok.AccessLevel.PRIVATE)
public class NotificationResponse {
    Integer id;
    String title;
    String content;
    String type;
    Integer targetId;
    String createdAt;
    Boolean isRead;
    UserResponse sender;
}
