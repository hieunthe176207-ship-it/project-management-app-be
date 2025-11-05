package com.fpt.project.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fpt.project.dto.ResponseError;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.NoArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;

import java.io.IOException;

@NoArgsConstructor
public class CustomTokenAccessDeniedHandler implements AccessDeniedHandler {
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response, AccessDeniedException accessDeniedException) throws IOException, ServletException {
        response.setContentType("application/json");
        response.setStatus(HttpStatus.FORBIDDEN.value());
        ResponseError error = new ResponseError();
        error.setCode(HttpStatus.FORBIDDEN.value());
        error.setMessage("Cannot access this resource");
        objectMapper.writeValue(response.getWriter(), error);
    }
}
