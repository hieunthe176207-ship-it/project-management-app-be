package com.fpt.project.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fpt.project.dto.ResponseError;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.oauth2.server.resource.web.BearerTokenAuthenticationEntryPoint;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class CustomAuthenticationEntryPoint implements AuthenticationEntryPoint {

    private final AuthenticationEntryPoint delegate = new BearerTokenAuthenticationEntryPoint();
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException authException) throws IOException, ServletException {
        this.delegate.commence(request, response, authException);
        response.setContentType("application/json");
        response.setStatus(HttpStatus.UNAUTHORIZED.value());
        ResponseError error = new ResponseError();
        error.setCode(HttpStatus.UNAUTHORIZED.value());
        error.setMessage("Invalid Token");
        objectMapper.writeValue(response.getWriter(), error);
    }
}
