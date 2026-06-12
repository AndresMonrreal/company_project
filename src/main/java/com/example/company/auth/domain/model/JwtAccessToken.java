package com.example.company.auth.domain.model;

import java.time.Instant;
import java.util.Objects;

public record JwtAccessToken(
        String token,
        Instant expiresAt
) {

    public JwtAccessToken {
        if (token == null || token.isBlank()) {
            throw new IllegalArgumentException("JWT token is required");
        }
        expiresAt = Objects.requireNonNull(expiresAt, "JWT expiration is required");
    }
}
