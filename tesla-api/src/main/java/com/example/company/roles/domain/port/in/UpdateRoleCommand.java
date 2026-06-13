package com.example.company.roles.domain.port.in;

public record UpdateRoleCommand(
        String name,
        String description
) {
}
