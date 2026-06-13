package com.example.company.container_types.application.usecase;

import java.util.List;

import com.example.company.container_types.application.mapper.ContainerTypeResultMapper;
import com.example.company.container_types.domain.exception.ContainerTypeNotFoundException;
import com.example.company.container_types.domain.port.in.ContainerTypeResult;
import com.example.company.container_types.domain.port.in.GetContainerTypeUseCase;
import com.example.company.container_types.domain.port.out.ContainerTypeRepositoryPort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class GetContainerTypeService implements GetContainerTypeUseCase {

    private final ContainerTypeRepositoryPort containerTypeRepository;

    public GetContainerTypeService(ContainerTypeRepositoryPort containerTypeRepository) {
        this.containerTypeRepository = containerTypeRepository;
    }

    @Override
    public List<ContainerTypeResult> findAllActive() {
        return containerTypeRepository.findAllActiveOrderByNameAsc()
                .stream()
                .map(ContainerTypeResultMapper::toResult)
                .toList();
    }

    @Override
    public ContainerTypeResult findById(Long id) {
        return containerTypeRepository.findActiveById(id)
                .map(ContainerTypeResultMapper::toResult)
                .orElseThrow(() -> new ContainerTypeNotFoundException(id));
    }
}
