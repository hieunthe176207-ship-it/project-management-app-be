package com.fpt.project.dto.response;


import com.fpt.project.constant.Role;
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
    String role;

    public UserResponse(int id, String displayName, String email, String avatar, Role role) {
        this.id = id;
        this.displayName = displayName;
        this.email = email;
        this.avatar = avatar;
        this.role = role.getNhan();
    }

    public UserResponse(com.fpt.project.entity.User user) {
        this.id = user.getId();
        this.email = user.getEmail();
        this.displayName = user.getDisplayName();
        this.avatar = user.getAvatar();
    }


}
