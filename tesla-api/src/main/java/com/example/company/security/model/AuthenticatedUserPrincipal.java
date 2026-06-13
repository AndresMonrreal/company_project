package com.example.company.security.model;

import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

public final class AuthenticatedUserPrincipal {

    private final AuthenticatedUserContext context;

    public AuthenticatedUserPrincipal(AuthenticatedUserContext context) {
        this.context = Objects.requireNonNull(context, "Authenticated user context is required");
    }

    public Long userId() {
        return context.userId();
    }

    public String username() {
        return context.username();
    }

    public String role() {
        return context.role();
    }

    public Instant expiresAt() {
        return context.expiresAt();
    }

    public Collection<? extends GrantedAuthority> authorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_" + context.role()));
    }

    @Override
    public String toString() {
        return "AuthenticatedUserPrincipal[userId=%s, username=%s, role=%s, expiresAt=%s]"
                .formatted(userId(), username(), role(), expiresAt());
    }
}
