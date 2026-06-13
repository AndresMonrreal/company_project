package com.example.company.roles.domain.port.in;

public record RoleResult(
        Long id,
        String name,
        String description,
        boolean active
) {
}
