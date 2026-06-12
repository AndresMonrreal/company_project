package com.example.company.auth.adapter.in.web.dto;

import java.time.LocalDateTime;

public record AuthErrorResponse(
        LocalDateTime timestamp,
        int status,
        String error,
        String code,
        String message,
        String path
) {
}
