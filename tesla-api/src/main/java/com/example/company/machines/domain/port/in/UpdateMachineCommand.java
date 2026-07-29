package com.example.company.machines.domain.port.in;

import java.time.LocalDate;

public record UpdateMachineCommand(
        String name,
        String code,
        String status,
        String processesType,
        Double cycleTimeSeconds,
        LocalDate lastMaintenanceDate,
        String observations
) {
}
