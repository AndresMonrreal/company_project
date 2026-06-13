package com.example.company.roles.adapter.in.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RoleCreateRequest(
        @NotBlank @Size(max = 80) String name,
        @Size(max = 255) String description
) {
}
