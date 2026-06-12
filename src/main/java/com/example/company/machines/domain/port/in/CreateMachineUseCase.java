package com.example.company.machines.domain.port.in;

public interface CreateMachineUseCase {

    MachineResult create(CreateMachineCommand command);
}
