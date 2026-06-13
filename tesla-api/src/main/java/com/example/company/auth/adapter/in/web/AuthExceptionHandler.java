package com.example.company.auth.adapter.in.web;

import java.time.LocalDateTime;

import com.example.company.auth.adapter.in.web.dto.AuthErrorResponse;
import com.example.company.auth.domain.exception.InactiveRoleException;
import com.example.company.auth.domain.exception.InactiveUserException;
import com.example.company.auth.domain.exception.InvalidCredentialsException;
import com.example.company.auth.domain.exception.RoleUnavailableException;
import com.example.company.auth.domain.exception.TokenConfigurationException;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice(assignableTypes = AuthRestController.class)
public class AuthExceptionHandler {

    private static final String VALIDATION_ERROR_CODE = "auth.validation-error";

    @ExceptionHandler(InvalidCredentialsException.class)
    ResponseEntity<AuthErrorResponse> invalidCredentials(
            InvalidCredentialsException ex,
            HttpServletRequest request
    ) {
        return build(HttpStatus.UNAUTHORIZED, ex.code(), ex.getMessage(), request);
    }

    @ExceptionHandler(InactiveUserException.class)
    ResponseEntity<AuthErrorResponse> inactiveUser(
            InactiveUserException ex,
            HttpServletRequest request
    ) {
        return build(HttpStatus.FORBIDDEN, ex.code(), ex.getMessage(), request);
    }

    @ExceptionHandler(InactiveRoleException.class)
    ResponseEntity<AuthErrorResponse> inactiveRole(
            InactiveRoleException ex,
            HttpServletRequest request
    ) {
        return build(HttpStatus.FORBIDDEN, ex.code(), ex.getMessage(), request);
    }

    @ExceptionHandler(RoleUnavailableException.class)
    ResponseEntity<AuthErrorResponse> roleUnavailable(
            RoleUnavailableException ex,
            HttpServletRequest request
    ) {
        return build(HttpStatus.FORBIDDEN, ex.code(), ex.getMessage(), request);
    }

    @ExceptionHandler(TokenConfigurationException.class)
    ResponseEntity<AuthErrorResponse> tokenConfiguration(
            TokenConfigurationException ex,
            HttpServletRequest request
    ) {
        return build(HttpStatus.INTERNAL_SERVER_ERROR, ex.code(), ex.getMessage(), request);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    ResponseEntity<AuthErrorResponse> validation(
            MethodArgumentNotValidException ex,
            HttpServletRequest request
    ) {
        return build(HttpStatus.BAD_REQUEST, VALIDATION_ERROR_CODE, "Invalid authentication request", request);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    ResponseEntity<AuthErrorResponse> malformedJson(
            HttpMessageNotReadableException ex,
            HttpServletRequest request
    ) {
        return build(HttpStatus.BAD_REQUEST, VALIDATION_ERROR_CODE, "Invalid authentication request", request);
    }

    private ResponseEntity<AuthErrorResponse> build(
            HttpStatus status,
            String code,
            String message,
            HttpServletRequest request
    ) {
        return ResponseEntity.status(status).body(new AuthErrorResponse(
                LocalDateTime.now(),
                status.value(),
                status.getReasonPhrase(),
                code,
                message,
                request.getRequestURI()
        ));
    }
}
