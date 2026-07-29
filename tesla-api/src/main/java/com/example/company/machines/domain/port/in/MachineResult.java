package com.example.company.machines.domain.port.in;

import java.time.LocalDate;

public record MachineResult(
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
