package com.example.company.security.adapter.in.web;

public record SecurityErrorResponse(
        String timestamp,
        int status,
        String error,
        String code,
        String message,
        String path
) {
}
