package com.fpt.project.controller;

import com.fpt.project.dto.request.LoginRequest;
import com.fpt.project.dto.request.RegisterRequest;
import com.fpt.project.dto.ResponseSuccess;
import com.fpt.project.exception.ApiException;
import com.fpt.project.service.AuthenticationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthenticationController {
    private final AuthenticationService authenticationService;

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody RegisterRequest data) throws ApiException {
        authenticationService.signUp(data);
        return ResponseEntity.ok(ResponseSuccess.builder()
                .code(200)
                .message("Đăng ký thành công")
                .build());
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest data) throws ApiException {
        return ResponseEntity.ok(ResponseSuccess.builder()
                .code(200)
                .message("Đăng nhập thành công")
                .data(authenticationService.login(data))
                .build());
    }
}
