package com.fpt.project.service.impl;

import com.fpt.project.dto.response.UserResponse;
import com.fpt.project.entity.User;
import com.fpt.project.exception.ApiException;
import com.fpt.project.repository.UserRepository;
import com.fpt.project.service.UserService;
import com.fpt.project.util.SecurityUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final SecurityUtil securityUtil;

    @Override
    public UserResponse getAccount() throws ApiException {
        String email = securityUtil.getEmailRequest();
        if (email == null) {
            throw new ApiException(400, "Tài khoản không tồn tại");
        }
        User user = userRepository.findByEmail(email);
        return UserResponse.builder()
                .id(user.getId())
                .email(user.getEmail())
                .displayName(user.getDisplayName())
                .build();
    }
}
