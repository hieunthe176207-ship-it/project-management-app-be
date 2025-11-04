package com.fpt.project.dto.response;


import lombok.*;
import lombok.experimental.FieldDefaults;

@FieldDefaults(level = AccessLevel.PRIVATE)
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UserResponse {
    int id;
    String displayName;
    String email;
    String password;
    String avatar;

    public UserResponse(com.fpt.project.entity.User user) {
        this.id = user.getId();
        this.email = user.getEmail();
        this.displayName = user.getDisplayName();
        this.avatar = user.getAvatar();
    }
}
