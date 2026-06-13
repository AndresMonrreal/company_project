package com.example.company.security.model;

import java.time.Instant;
import java.util.Objects;

public record AuthenticatedUserContext(
        Long userId,
        String username,
        String role,
        Instant expiresAt
) {

    public AuthenticatedUserContext {
        userId = Objects.requireNonNull(userId, "User id is required");
        username = requiredText(username, "Username is required");
        role = requiredText(role, "Role is required");
        expiresAt = Objects.requireNonNull(expiresAt, "Token expiration is required");
    }

    private static String requiredText(String value, String message) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(message);
        }
        return value.trim();
    }
}
