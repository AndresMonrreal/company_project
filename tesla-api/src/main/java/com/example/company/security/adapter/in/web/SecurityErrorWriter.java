package com.example.company.security.adapter.in.web;

import java.io.IOException;
import java.time.Instant;

import com.example.company.security.model.JwtValidationException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;

@Component
public class SecurityErrorWriter {

    private static final String MISSING_TOKEN_CODE = "security.missing-token";
    private static final String MISSING_TOKEN_MESSAGE = "Authentication token is required";
    private static final String FORBIDDEN_CODE = "security.forbidden";
    private static final String FORBIDDEN_MESSAGE = "Insufficient permissions";

    private final ObjectMapper objectMapper = new ObjectMapper();

    public void missingToken(HttpServletRequest request, HttpServletResponse response) throws IOException {
        write(response, request, HttpStatus.UNAUTHORIZED, MISSING_TOKEN_CODE, MISSING_TOKEN_MESSAGE);
    }

    public void invalidToken(
            HttpServletRequest request,
            HttpServletResponse response,
            JwtValidationException exception
    ) throws IOException {
        write(response, request, HttpStatus.UNAUTHORIZED, exception.reason().code(), exception.getMessage());
    }

    public void forbidden(HttpServletRequest request, HttpServletResponse response) throws IOException {
        write(response, request, HttpStatus.FORBIDDEN, FORBIDDEN_CODE, FORBIDDEN_MESSAGE);
    }

    private void write(
            HttpServletResponse response,
            HttpServletRequest request,
            HttpStatus status,
            String code,
            String message
    ) throws IOException {
        if (response.isCommitted()) {
            return;
        }

        response.setStatus(status.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        objectMapper.writeValue(response.getWriter(), new SecurityErrorResponse(
                Instant.now().toString(),
                status.value(),
                status.getReasonPhrase(),
                code,
                message,
                request.getRequestURI()
        ));
    }
}
