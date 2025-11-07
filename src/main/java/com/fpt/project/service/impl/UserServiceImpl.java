package com.fpt.project.service.impl;

import com.fpt.project.dto.request.UpdateAccountRequest;
import com.fpt.project.dto.response.UserResponse;
import com.fpt.project.entity.User;
import com.fpt.project.exception.ApiException;
import com.fpt.project.repository.ProjectMemberRepository;
import com.fpt.project.repository.UserRepository;
import com.fpt.project.service.UserService;
import com.fpt.project.util.SecurityUtil;
import com.fpt.project.util.Util;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final SecurityUtil securityUtil;
    private final ProjectMemberRepository projectMemberRepository;

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
                .avatar(user.getAvatar())
                .build();
    }

    @Override
    public UserResponse updateAccount(UpdateAccountRequest data) throws ApiException, IOException {
        String email = securityUtil.getEmailRequest();
        if (email == null) {
            throw new ApiException(400, "Tài khoản không tồn tại");
        }
        User user = userRepository.findByEmail(email);
        if (data.getDisplayName() != null) {
            user.setDisplayName(data.getDisplayName());
        }
        if (data.getAvatar() != null) {
            String url = Util.uploadImage(data.getAvatar());
            user.setAvatar(url);
        }
        userRepository.save(user);
        return UserResponse.builder()
                .id(user.getId())
                .email(user.getEmail())
                .displayName(user.getDisplayName())
                .avatar(user.getAvatar())
                .build();
    }

    @Override
    public List<UserResponse> getAllUsers(int projectId) throws ApiException {
        List<User> users = projectMemberRepository.findUsersWithoutMember(projectId);
        return users.stream().map(user -> UserResponse.builder()
                .id(user.getId())
                .email(user.getEmail())
                .displayName(user.getDisplayName())
                .avatar(user.getAvatar())
                .build()).toList();
    }

    @Override
    public void updateTokenFcm(String token) {
        String email = securityUtil.getEmailRequest();
        if (email == null) {
            return;
        }
        User user = userRepository.findByEmail(email);
        user.setTokenFcm(token);
        userRepository.save(user);
    }
}
