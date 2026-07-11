package com.example.company.profiles.adapter.in.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record ProfileCreateRequest(
        @NotBlank @Size(max = 10) String code,
        @NotBlank @Size(max = 100) String name,
        @Size(max = 255) String description,
        @NotBlank @Pattern(regexp = "HEADER|LOWER") String type,
        @NotBlank @Pattern(regexp = "FRONT|REAR") String position
) {
}
