package com.fpt.project.service;

import com.fpt.project.dto.response.UserResponse;
import com.fpt.project.exception.ApiException;

public interface UserService {
    public UserResponse getAccount() throws ApiException;
}
