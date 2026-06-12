package com.example.company.container_types.adapter.in.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ContainerTypeCreateRequest(
        @NotBlank @Size(max = 80) String name
) {
}
