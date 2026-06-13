package com.example.company.containers.application.usecase;

import java.util.List;

import com.example.company.containers.application.mapper.ContainerResultMapper;
import com.example.company.containers.domain.exception.ContainerNotFoundException;
import com.example.company.containers.domain.port.in.ContainerResult;
import com.example.company.containers.domain.port.in.GetContainerUseCase;
import com.example.company.containers.domain.port.out.ContainerRepositoryPort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class GetContainerService implements GetContainerUseCase {

    private final ContainerRepositoryPort containerRepository;

    public GetContainerService(ContainerRepositoryPort containerRepository) {
        this.containerRepository = containerRepository;
    }

    @Override
    public List<ContainerResult> findAllActive() {
        return containerRepository.findAllActiveOrderByCodeAsc()
                .stream()
                .map(ContainerResultMapper::toResult)
                .toList();
    }

    @Override
    public ContainerResult findById(Long id) {
        return containerRepository.findActiveById(id)
                .map(ContainerResultMapper::toResult)
                .orElseThrow(() -> new ContainerNotFoundException(id));
    }
}
