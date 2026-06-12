package com.example.company.roles.domain.port.in;

public record CreateRoleCommand(
        String name,
        String description
) {
}
