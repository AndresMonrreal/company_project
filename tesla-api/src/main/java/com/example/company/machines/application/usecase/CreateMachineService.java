package com.example.company.machines.application.usecase;

import com.example.company.machines.application.mapper.MachineResultMapper;
import com.example.company.machines.domain.exception.DuplicateMachineCodeException;
import com.example.company.machines.domain.exception.DuplicateMachineNameException;
import com.example.company.machines.domain.model.Machine;
import com.example.company.machines.domain.port.in.CreateMachineCommand;
import com.example.company.machines.domain.port.in.CreateMachineUseCase;
import com.example.company.machines.domain.port.in.MachineResult;
import com.example.company.machines.domain.port.out.MachineRepositoryPort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CreateMachineService implements CreateMachineUseCase {

    private final MachineRepositoryPort machineRepository;

    public CreateMachineService(MachineRepositoryPort machineRepository) {
        this.machineRepository = machineRepository;
    }

    @Override
    @Transactional
    public MachineResult create(CreateMachineCommand command) {
        if (machineRepository.existsByName(command.name())) {
            throw new DuplicateMachineNameException(command.name());
        }

        if (command.code() != null && machineRepository.existsByCode(command.code())) {
            throw new DuplicateMachineCodeException(command.code());
        }

        Machine machine = Machine.create(command.name(), command.code(), command.status(),
                command.processesType(), command.cycleTimeSeconds(),
                command.lastMaintenanceDate(), command.observations());
        return MachineResultMapper.toResult(machineRepository.save(machine));
    }
}
