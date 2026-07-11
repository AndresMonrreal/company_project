package com.example.company.machines.domain.port.in;

import java.time.LocalDate;

public record CreateMachineCommand(
        String name,
        String code,
        String status,
        String processesType,
        Integer cycleTimeSeconds,
        LocalDate lastMaintenanceDate,
        String observations
) {
}
