package com.example.company.containers.application.usecase;

import com.example.company.containers.application.mapper.ContainerResultMapper;
import com.example.company.containers.domain.exception.DuplicateContainerCodeException;
import com.example.company.containers.domain.model.Container;
import com.example.company.containers.domain.port.in.ContainerResult;
import com.example.company.containers.domain.port.in.CreateContainerCommand;
import com.example.company.containers.domain.port.in.CreateContainerUseCase;
import com.example.company.containers.domain.port.out.ContainerRepositoryPort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CreateContainerService implements CreateContainerUseCase {

    private final ContainerRepositoryPort containerRepository;

    public CreateContainerService(ContainerRepositoryPort containerRepository) {
        this.containerRepository = containerRepository;
    }

    @Override
    @Transactional
    public ContainerResult create(CreateContainerCommand command) {
        if (containerRepository.existsByCode(command.code())) {
            throw new DuplicateContainerCodeException(command.code());
        }

        Container container = Container.create(command.containerTypeId(), command.code());
        return ContainerResultMapper.toResult(containerRepository.save(container));
    }
}
