package com.example.company.machines.application.usecase;

import com.example.company.machines.domain.exception.MachineNotFoundException;
import com.example.company.machines.domain.model.Machine;
import com.example.company.machines.domain.port.in.DeleteMachineUseCase;
import com.example.company.machines.domain.port.out.MachineRepositoryPort;
import com.example.company.shared.domain.exception.DomainException;
import com.example.company.shared.domain.exception.DomainErrorType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class DeleteMachineService implements DeleteMachineUseCase {

    private final MachineRepositoryPort machineRepository;

    public DeleteMachineService(MachineRepositoryPort machineRepository) {
        this.machineRepository = machineRepository;
    }

    @Override
    @Transactional
    public void delete(Long id) {
        Machine machine = machineRepository.findActiveById(id)
                .orElseThrow(() -> new MachineNotFoundException(id));

        if (machineRepository.hasActiveCuttingRecords(id)) {
            throw new DomainException(DomainErrorType.CONFLICT, "machine.has-cutting-records", "Cannot delete machine because it has associated cutting records.") {};
        }

        machine.deactivate();
        machineRepository.save(machine);
    }
}
