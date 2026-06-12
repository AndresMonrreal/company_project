package com.example.company.security_bootstrap.domain.model;

import java.util.Objects;

public record BootstrapUserDefinition(
        String username,
        String fullName,
        BootstrapRoleName roleName,
        String rawPassword,
        boolean demo
) {

    private static final int MAX_USERNAME_LENGTH = 80;
    private static final int MAX_FULL_NAME_LENGTH = 120;

    public BootstrapUserDefinition {
        username = requiredText(username, "Bootstrap username is required", MAX_USERNAME_LENGTH, "Bootstrap username");
        fullName = requiredText(fullName, "Bootstrap full name is required", MAX_FULL_NAME_LENGTH, "Bootstrap full name");
        roleName = Objects.requireNonNull(roleName, "Bootstrap role is required");

        if (rawPassword == null || rawPassword.isBlank()) {
            throw new IllegalArgumentException("Bootstrap user password is required for " + username);
        }
    }

    private static String requiredText(String value, String requiredMessage, int maxLength, String label) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(requiredMessage);
        }

        String trimmed = value.trim();
        if (trimmed.length() > maxLength) {
            throw new IllegalArgumentException(label + " must be " + maxLength + " characters or fewer");
        }

        return trimmed;
    }

    @Override
    public String toString() {
        return "BootstrapUserDefinition[username=%s, fullName=%s, roleName=%s, rawPassword=<redacted>, demo=%s]"
                .formatted(username, fullName, roleName, demo);
    }
}
