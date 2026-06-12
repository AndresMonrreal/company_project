package com.example.company.auth.domain.port.in;

import java.time.Instant;
import java.util.Objects;

import com.example.company.auth.domain.model.AuthenticatedUserSummary;

public record LoginResult(
        String accessToken,
        String tokenType,
        Instant expiresAt,
        AuthenticatedUserSummary user
) {

    public LoginResult {
        if (accessToken == null || accessToken.isBlank()) {
            throw new IllegalArgumentException("Access token is required");
        }
        if (tokenType == null || tokenType.isBlank()) {
            throw new IllegalArgumentException("Token type is required");
        }
        expiresAt = Objects.requireNonNull(expiresAt, "Token expiration is required");
        user = Objects.requireNonNull(user, "Authenticated user is required");
    }
}
