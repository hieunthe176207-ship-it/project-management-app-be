package com.fpt.project.service.impl;

import com.fpt.project.entity.User;
import com.fpt.project.exception.ApiException;
import com.fpt.project.service.AuthenticationService;
import com.fpt.project.dto.response.TokenResponse;
import com.fpt.project.dto.response.UserResponse;
import com.fpt.project.dto.request.LoginRequest;
import com.fpt.project.dto.request.RegisterRequest;
import com.fpt.project.dto.response.LoginResponse;
import com.fpt.project.repository.UserRepository;
import com.fpt.project.util.SecurityUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthenticationServiceImpl implements AuthenticationService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final SecurityUtil securityUtil;

    @Override
    public LoginResponse login(LoginRequest loginRequest) throws ApiException {
        User user = userRepository.findByEmail(loginRequest.getEmail());
        if (user == null) {
            // throw looix
            throw new ApiException(400, "Tài khoản không tồn tại");
        }

        if (!passwordEncoder.matches(loginRequest.getPassword(), user.getPassword())) {
            // throw loi
            throw new ApiException(400, "Mật khẩu không đúng");
        }

        String token = securityUtil.createToken(user);

        // user
        // token
        TokenResponse tokenResponse = TokenResponse.builder()
                .accessToken(token)
                .build();
        UserResponse userResponse = UserResponse.builder()
                .id(user.getId())
                .email(user.getEmail())
                .displayName(user.getDisplayName())
                .build();
        return LoginResponse.builder()
                .user(userResponse)
                .token(tokenResponse)
                .build();
    }

    @Override
    public void signUp(RegisterRequest registerRequest) throws ApiException {

        //check , uncheck
        try {
            if (!registerRequest.getPassword().equals(registerRequest.getConfirmPassword()))
                throw new ApiException(400, "Mật khẩu xác nhận không khớp" );
            String encodePassword = passwordEncoder.encode(registerRequest.getPassword());
            User u = new User();
            u.setEmail(registerRequest.getEmail());
            u.setPassword(encodePassword);
            u.setDisplayName(registerRequest.getDisplayName());
            userRepository.save(u);
        } catch (DataIntegrityViolationException e) {
            throw new ApiException(400, "User đã tồn tại");
        }
    }
}
