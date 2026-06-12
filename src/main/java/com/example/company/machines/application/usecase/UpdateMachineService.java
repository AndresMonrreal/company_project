package com.example.company.machines.application.usecase;

import com.example.company.machines.application.mapper.MachineResultMapper;
import com.example.company.machines.domain.exception.DuplicateMachineNameException;
import com.example.company.machines.domain.exception.MachineNotFoundException;
import com.example.company.machines.domain.model.Machine;
import com.example.company.machines.domain.port.in.MachineResult;
import com.example.company.machines.domain.port.in.UpdateMachineCommand;
import com.example.company.machines.domain.port.in.UpdateMachineUseCase;
import com.example.company.machines.domain.port.out.MachineRepositoryPort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UpdateMachineService implements UpdateMachineUseCase {

    private final MachineRepositoryPort machineRepository;

    public UpdateMachineService(MachineRepositoryPort machineRepository) {
        this.machineRepository = machineRepository;
    }

    @Override
    @Transactional
    public MachineResult update(Long id, UpdateMachineCommand command) {
        Machine machine = machineRepository.findActiveById(id)
                .orElseThrow(() -> new MachineNotFoundException(id));

        if (machineRepository.existsByNameAndIdNot(command.name(), id)) {
            throw new DuplicateMachineNameException(command.name());
        }

        machine.update(command.name());
        return MachineResultMapper.toResult(machineRepository.save(machine));
    }
}
