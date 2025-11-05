package com.fpt.project.exception;


import com.fpt.project.dto.ResponseError;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authorization.AuthorizationDeniedException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.resource.NoResourceFoundException;

@RestControllerAdvice
public class HandleException {
    @ExceptionHandler(value = {BadCredentialsException.class, UsernameNotFoundException.class})
    public ResponseEntity<ResponseError> handleException(Exception e) {
        ResponseError responseError = new ResponseError();
        responseError.setMessage(e.getMessage());
        responseError.setCode(HttpStatus.BAD_REQUEST.value());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(responseError);
    }

    @ExceptionHandler(value = NoResourceFoundException.class)
    public ResponseEntity<ResponseError> handleException(NoResourceFoundException e) {
        ResponseError responseError = new ResponseError();
        responseError.setMessage(e.getMessage());
        responseError.setCode(HttpStatus.NOT_FOUND.value());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(responseError);
    }


    @ExceptionHandler(value = ApiException.class)
    public ResponseEntity<ResponseError> handleException(ApiException e) {
        ResponseError responseError = new ResponseError();
        responseError.setMessage(e.getMessage());
        responseError.setCode(e.getCode());
        return ResponseEntity.status(e.getCode()).body(responseError);
    }

    @ExceptionHandler(value = RuntimeException.class)
    public ResponseEntity<ResponseError> handleException(RuntimeException e) {
        ResponseError responseError = new ResponseError();
        responseError.setMessage(e.getMessage());
        responseError.setCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR.value()).body(responseError);
    }

    @ExceptionHandler(value = AuthorizationDeniedException.class)
    public ResponseEntity<ResponseError> handleException(AuthorizationDeniedException e) {
        ResponseError responseError = new ResponseError();
        responseError.setMessage("Không có quyền truy cập ");
        responseError.setCode(HttpStatus.FORBIDDEN.value());
        return ResponseEntity.status(HttpStatus.FORBIDDEN.value()).body(responseError);
    }

    @ExceptionHandler(value = MethodArgumentNotValidException.class)
    public ResponseEntity<ResponseError> handleException(MethodArgumentNotValidException e) {
        ResponseError responseError = new ResponseError();
        String fullMessage = e.getMessage();
        String[] parts = fullMessage.split("default message \\[");
        String message = parts[parts.length - 1].split("]]")[0];
        responseError.setMessage(message.trim());
        responseError.setCode(HttpStatus.BAD_REQUEST.value());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST.value()).body(responseError);
    }
}
