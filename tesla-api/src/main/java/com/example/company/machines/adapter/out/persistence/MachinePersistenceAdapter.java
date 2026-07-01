package com.example.company.machines.adapter.out.persistence;

import java.util.List;
import java.util.Optional;

import com.example.company.machines.domain.model.Machine;
import com.example.company.machines.domain.port.out.MachineRepositoryPort;
import org.springframework.stereotype.Repository;

@Repository
public class MachinePersistenceAdapter implements MachineRepositoryPort {

    private final SpringDataMachineRepository machineRepository;
    private final MachinePersistenceMapper mapper;

    public MachinePersistenceAdapter(
            SpringDataMachineRepository machineRepository,
            MachinePersistenceMapper mapper
    ) {
        this.machineRepository = machineRepository;
        this.mapper = mapper;
    }

    @Override
    public List<Machine> findAllActiveOrderByNameAsc() {
        return machineRepository.findByActiveTrueOrderByNameAsc()
                .stream()
                .map(mapper::toDomain)
                .toList();
    }

    @Override
    public Optional<Machine> findActiveById(Long id) {
        return machineRepository.findByIdAndActiveTrue(id).map(mapper::toDomain);
    }

    @Override
    public boolean existsByName(String name) {
        return machineRepository.existsByName(name);
    }

    @Override
    public boolean existsByNameAndIdNot(String name, Long id) {
        return machineRepository.existsByNameAndIdNot(name, id);
    }

    @Override
    public boolean hasActiveCuttingRecords(Long machineId) {
        return machineRepository.hasActiveCuttingRecords(machineId);
    }

    @Override
    public Machine save(Machine machine) {
        MachineJpaEntity entity = machine.id() == null
                ? mapper.toNewEntity(machine)
                : machineRepository.findById(machine.id())
                        .orElseThrow(() -> new IllegalStateException(
                                "Machine entity disappeared during save: " + machine.id()
                        ));

        entity.updateFromDomain(machine.name(), machine.active());
        return mapper.toDomain(machineRepository.save(entity));
    }
}
