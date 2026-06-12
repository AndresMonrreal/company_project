package com.example.company.roles.adapter.in.web.dto;

public record RoleResponse(
        Long id,
        String name,
        String description,
        boolean active
) {
}
