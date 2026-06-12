package com.example.company.auth.domain.model;

public record LoginCredentials(
        String username,
        String password
) {

    public LoginCredentials {
        if (username == null || username.isBlank()) {
            throw new IllegalArgumentException("Username is required");
        }
        if (password == null || password.isBlank()) {
            throw new IllegalArgumentException("Password is required");
        }

        username = username.trim();
    }

    @Override
    public String toString() {
        return "LoginCredentials[username=%s, password=<redacted>]".formatted(username);
    }
}
