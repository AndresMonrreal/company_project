package com.example.company.machines.domain.port.in;

public interface UpdateMachineUseCase {

    MachineResult update(Long id, UpdateMachineCommand command);
}
