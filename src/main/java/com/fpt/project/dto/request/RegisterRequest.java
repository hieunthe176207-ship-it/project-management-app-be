package com.fpt.project.dto.request;

import com.fpt.project.dto.response.UserResponse;
import lombok.*;
import lombok.experimental.FieldDefaults;

@EqualsAndHashCode(callSuper = true)
@FieldDefaults(level = AccessLevel.PRIVATE)
@Data
@AllArgsConstructor
@NoArgsConstructor
public class RegisterRequest extends UserResponse {
    String confirmPassword;

}
