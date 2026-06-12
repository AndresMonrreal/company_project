package com.example.company.machines.adapter.out.persistence;

import com.example.company.machines.domain.model.Machine;
import org.springframework.stereotype.Component;

@Component
public class MachinePersistenceMapper {

    Machine toDomain(MachineJpaEntity entity) {
        return Machine.restore(
                entity.getId(),
                entity.getName(),
                entity.isActive()
        );
    }

    MachineJpaEntity toNewEntity(Machine machine) {
        return new MachineJpaEntity(
                machine.name(),
                machine.active()
        );
    }
}
