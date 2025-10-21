package com.fpt.project.controller;

import com.fpt.project.dto.ResponseSuccess;
import com.fpt.project.dto.response.UserResponse;
import com.fpt.project.exception.ApiException;
import com.fpt.project.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/user")
public class UserController {

    private final UserService userService;

    @GetMapping("/account")
    public ResponseEntity<ResponseSuccess<UserResponse>> getAccount() throws ApiException {
        UserResponse userResponse = userService.getAccount();
        return ResponseEntity.ok(ResponseSuccess.<UserResponse>builder()
                .code(200)
                .message("Lấy thông tin tài khoản thành công")
                .data(userResponse)
                .build());
    }
}
