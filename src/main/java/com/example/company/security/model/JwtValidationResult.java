package com.example.company.security.model;

import java.util.Objects;

public record JwtValidationResult(
        AuthenticatedUserContext userContext
) {

    public JwtValidationResult {
        userContext = Objects.requireNonNull(userContext, "Authenticated user context is required");
    }
}
