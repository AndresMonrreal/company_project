package com.example.company.machines.adapter.out.persistence;

import com.example.company.machines.domain.model.Machine;
import org.springframework.stereotype.Component;

@Component
public class MachinePersistenceMapper {

    Machine toDomain(MachineJpaEntity entity) {
        return Machine.restore(
                entity.getId(),
                entity.getName(),
                entity.isActive(),
                entity.getCode(),
                entity.getStatus(),
                entity.getProcessesType(),
                entity.getCycleTimeSeconds(),
                entity.getLastMaintenanceDate(),
                entity.getObservations()
        );
    }

    MachineJpaEntity toNewEntity(Machine machine) {
        return new MachineJpaEntity(
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
