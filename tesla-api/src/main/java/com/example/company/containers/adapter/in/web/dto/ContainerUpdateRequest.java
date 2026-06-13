package com.example.company.containers.adapter.in.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record ContainerUpdateRequest(
        @NotNull Long containerTypeId,
        @NotBlank @Size(max = 80) String code
) {
}
