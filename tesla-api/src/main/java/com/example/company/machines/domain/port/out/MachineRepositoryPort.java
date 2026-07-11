package com.example.company.machines.domain.port.out;

import java.util.List;
import java.util.Optional;

import com.example.company.machines.domain.model.Machine;

public interface MachineRepositoryPort {

    List<Machine> findAllActiveOrderByNameAsc();

    Optional<Machine> findActiveById(Long id);

    boolean existsByName(String name);

    boolean existsByNameAndIdNot(String name, Long id);

    boolean hasActiveCuttingRecords(Long machineId);

    boolean existsByCode(String code);

    boolean existsByCodeAndIdNot(String code, Long id);

    Machine save(Machine machine);
}
