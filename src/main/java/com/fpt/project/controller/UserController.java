package com.fpt.project.controller;

import com.fpt.project.dto.ResponseSuccess;
import com.fpt.project.dto.request.ChangePasswordRequest;
import com.fpt.project.dto.request.UpdateAccountRequest;
import com.fpt.project.dto.response.UserResponse;
import com.fpt.project.exception.ApiException;
import com.fpt.project.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

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

    @PutMapping(value = "/update", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> updateAccount(@ModelAttribute UpdateAccountRequest data) throws ApiException, IOException {
        return ResponseEntity.ok(ResponseSuccess.builder()
                .code(200)
                .message("Cập nhật thông tin tài khoản thành công")
                .data(userService.updateAccount(data))
                .build());
    }

    @GetMapping("/get-all/{id}")
    public ResponseEntity<ResponseSuccess<?>> getAllUsers(@PathVariable int id) throws ApiException {
        return ResponseEntity.ok(ResponseSuccess.builder()
                .code(200)
                .message("Lấy danh sách người dùng thành công")
                .data(userService.getAllUsers(id))
                .build());
    }

    @PostMapping("/update-token-fcm")
    public ResponseEntity<ResponseSuccess<?>> updateTokenFcm(@RequestParam String token) {
        userService.updateTokenFcm(token);
        return ResponseEntity.ok(ResponseSuccess.builder()
                .code(200)
                .message("Cập nhật token FCM thành công")
                .build());
    }

    @PutMapping("/change-password")
    public ResponseEntity<ResponseSuccess<?>> changePassword(@RequestBody ChangePasswordRequest data) throws ApiException {
        userService.changePassword(data);
        return ResponseEntity.ok(ResponseSuccess.builder()
                .code(200)
                .message("Đổi mật khẩu thành công")
                .build());
    }


}
