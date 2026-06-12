package com.example.company.auth.adapter.in.web.dto;

public record AuthenticatedUserResponse(
        Long userId,
        String username,
        String fullName,
        String role
) {
}
