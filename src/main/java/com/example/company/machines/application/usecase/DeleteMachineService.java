package com.example.company.machines.application.usecase;

import com.example.company.machines.domain.exception.MachineNotFoundException;
import com.example.company.machines.domain.model.Machine;
import com.example.company.machines.domain.port.in.DeleteMachineUseCase;
import com.example.company.machines.domain.port.out.MachineRepositoryPort;
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

        machine.deactivate();
        machineRepository.save(machine);
    }
}
