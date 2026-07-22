package com.example.company.machines.adapter.in.web.dto;

import java.time.LocalDate;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record MachineUpdateRequest(
        @NotBlank @Size(max = 80) String name,
        @Size(max = 20) String code,
        @NotBlank @Size(max = 20) String status,
        @Pattern(regexp = "^(HEADER|LOWER|BOTH)$") String processesType,
        Integer cycleTimeSeconds,
        LocalDate lastMaintenanceDate,
        @Size(max = 500) String observations
) {
}
