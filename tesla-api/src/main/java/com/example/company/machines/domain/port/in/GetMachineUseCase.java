package com.example.company.machines.domain.port.in;

import java.util.List;

public interface GetMachineUseCase {

    List<MachineResult> findAllActive();

    MachineResult findById(Long id);
}
