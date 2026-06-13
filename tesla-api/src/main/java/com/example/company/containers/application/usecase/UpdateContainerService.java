package com.example.company.containers.application.usecase;

import com.example.company.containers.application.mapper.ContainerResultMapper;
import com.example.company.containers.domain.exception.ContainerNotFoundException;
import com.example.company.containers.domain.exception.DuplicateContainerCodeException;
import com.example.company.containers.domain.model.Container;
import com.example.company.containers.domain.port.in.ContainerResult;
import com.example.company.containers.domain.port.in.UpdateContainerCommand;
import com.example.company.containers.domain.port.in.UpdateContainerUseCase;
import com.example.company.containers.domain.port.out.ContainerRepositoryPort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UpdateContainerService implements UpdateContainerUseCase {

    private final ContainerRepositoryPort containerRepository;

    public UpdateContainerService(ContainerRepositoryPort containerRepository) {
        this.containerRepository = containerRepository;
    }

    @Override
    @Transactional
    public ContainerResult update(Long id, UpdateContainerCommand command) {
        Container container = containerRepository.findActiveById(id)
                .orElseThrow(() -> new ContainerNotFoundException(id));

        if (containerRepository.existsByCodeAndIdNot(command.code(), id)) {
            throw new DuplicateContainerCodeException(command.code());
        }

        container.update(command.containerTypeId(), command.code());
        return ContainerResultMapper.toResult(containerRepository.save(container));
    }
}
