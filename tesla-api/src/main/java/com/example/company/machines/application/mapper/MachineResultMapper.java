package com.example.company.machines.application.mapper;

import com.example.company.machines.domain.model.Machine;
import com.example.company.machines.domain.port.in.MachineResult;

public final class MachineResultMapper {

    private MachineResultMapper() {
    }

    public static MachineResult toResult(Machine machine) {
        return new MachineResult(
                machine.id(),
                machine.name(),
                machine.active(),
                machine.code(),
                machine.status(),
                machine.processesType(),
                machine.cycleTimeSeconds(),
                machine.lastMaintenanceDate(),
                machine.observations()
        );
    }
}
