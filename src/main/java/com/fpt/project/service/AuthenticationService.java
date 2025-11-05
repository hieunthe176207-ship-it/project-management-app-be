package com.fpt.project.service;


import com.fpt.project.exception.ApiException;
import com.fpt.project.dto.request.LoginRequest;
import com.fpt.project.dto.request.RegisterRequest;
import com.fpt.project.dto.response.LoginResponse;

//login
// user , token
public interface AuthenticationService {
    LoginResponse login(LoginRequest loginRequest) throws ApiException;

    void signUp(RegisterRequest registerRequest) throws ApiException;

}
