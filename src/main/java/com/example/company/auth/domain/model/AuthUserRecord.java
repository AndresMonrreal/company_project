package com.example.company.auth.domain.model;

import java.util.Objects;

public record AuthUserRecord(
        Long userId,
        String username,
        String fullName,
        String passwordHash,
        boolean active,
        String roleName,
        Boolean roleActive
) {

    public AuthUserRecord {
        userId = Objects.requireNonNull(userId, "User id is required");
        username = requiredText(username, "Username is required");
        fullName = requiredText(fullName, "Full name is required");
        passwordHash = requiredText(passwordHash, "Password hash is required");
    }

    public boolean hasRole() {
        return roleName != null && !roleName.isBlank() && roleActive != null;
    }

    @Override
    public String toString() {
        return "AuthUserRecord[userId=%s, username=%s, fullName=%s, passwordHash=<redacted>, active=%s, roleName=%s, roleActive=%s]"
                .formatted(userId, username, fullName, active, roleName, roleActive);
    }

    private static String requiredText(String value, String message) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(message);
        }
        return value.trim();
    }
}
