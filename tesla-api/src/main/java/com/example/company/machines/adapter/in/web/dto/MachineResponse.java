package com.example.company.machines.adapter.in.web.dto;

import java.time.LocalDate;

public record MachineResponse(
        Long id,
        String name,
        boolean active,
        String code,
        String status,
        String processesType,
        Double cycleTimeSeconds,
        LocalDate lastMaintenanceDate,
        String observations
) {
}
