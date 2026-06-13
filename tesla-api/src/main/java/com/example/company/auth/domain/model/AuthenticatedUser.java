package com.example.company.auth.domain.model;

import java.util.Objects;

public record AuthenticatedUser(
        Long userId,
        String username,
        String fullName,
        String role
) {

    public AuthenticatedUser {
        userId = Objects.requireNonNull(userId, "User id is required");
        username = requiredText(username, "Username is required");
        fullName = requiredText(fullName, "Full name is required");
        role = requiredText(role, "Role is required");
    }

    private static String requiredText(String value, String message) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(message);
        }
        return value.trim();
    }
}
