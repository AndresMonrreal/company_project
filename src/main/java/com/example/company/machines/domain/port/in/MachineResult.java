package com.example.company.machines.domain.port.in;

public record MachineResult(
        Long id,
        String name,
        boolean active
) {
}
