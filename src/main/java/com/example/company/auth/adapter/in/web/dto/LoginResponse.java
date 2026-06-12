package com.example.company.auth.adapter.in.web.dto;

import java.time.Instant;

public record LoginResponse(
        String accessToken,
        String tokenType,
        Instant expiresAt,
        AuthenticatedUserResponse user
) {
}
