package com.example.company.machines.adapter.in.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record MachineUpdateRequest(
        @NotBlank @Size(max = 80) String name
) {
}
