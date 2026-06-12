package com.example.company.machines.application.usecase;

import java.util.List;

import com.example.company.machines.application.mapper.MachineResultMapper;
import com.example.company.machines.domain.exception.MachineNotFoundException;
import com.example.company.machines.domain.port.in.GetMachineUseCase;
import com.example.company.machines.domain.port.in.MachineResult;
import com.example.company.machines.domain.port.out.MachineRepositoryPort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class GetMachineService implements GetMachineUseCase {

    private final MachineRepositoryPort machineRepository;

    public GetMachineService(MachineRepositoryPort machineRepository) {
        this.machineRepository = machineRepository;
    }

    @Override
    public List<MachineResult> findAllActive() {
        return machineRepository.findAllActiveOrderByNameAsc()
                .stream()
                .map(MachineResultMapper::toResult)
                .toList();
    }

    @Override
    public MachineResult findById(Long id) {
        return machineRepository.findActiveById(id)
                .map(MachineResultMapper::toResult)
                .orElseThrow(() -> new MachineNotFoundException(id));
    }
}
