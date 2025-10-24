package com.fpt.project.service;

import com.fpt.project.dto.request.UpdateAccountRequest;
import com.fpt.project.dto.response.UserResponse;
import com.fpt.project.exception.ApiException;

import java.io.IOException;

public interface UserService {
    public UserResponse getAccount() throws ApiException;
    UserResponse updateAccount(UpdateAccountRequest data) throws ApiException, IOException;
}
