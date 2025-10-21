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
}
