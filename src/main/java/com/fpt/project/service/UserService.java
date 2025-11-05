package com.fpt.project.service;

import com.fpt.project.dto.request.UpdateAccountRequest;
import com.fpt.project.dto.response.UserResponse;
import com.fpt.project.exception.ApiException;

import java.io.IOException;
import java.util.List;

public interface UserService {
    public UserResponse getAccount() throws ApiException;
    UserResponse updateAccount(UpdateAccountRequest data) throws ApiException, IOException;
    List<UserResponse> getAllUsers(int projectId) throws ApiException;

    void updateTokenFcm(String token);
}
