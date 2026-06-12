package com.example.company.container_types.application.usecase;

import com.example.company.container_types.application.mapper.ContainerTypeResultMapper;
import com.example.company.container_types.domain.exception.DuplicateContainerTypeNameException;
import com.example.company.container_types.domain.model.ContainerType;
import com.example.company.container_types.domain.port.in.ContainerTypeResult;
import com.example.company.container_types.domain.port.in.CreateContainerTypeCommand;
import com.example.company.container_types.domain.port.in.CreateContainerTypeUseCase;
import com.example.company.container_types.domain.port.out.ContainerTypeRepositoryPort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CreateContainerTypeService implements CreateContainerTypeUseCase {

    private final ContainerTypeRepositoryPort containerTypeRepository;

    public CreateContainerTypeService(ContainerTypeRepositoryPort containerTypeRepository) {
        this.containerTypeRepository = containerTypeRepository;
    }

    @Override
    @Transactional
    public ContainerTypeResult create(CreateContainerTypeCommand command) {
        if (containerTypeRepository.existsByName(command.name())) {
            throw new DuplicateContainerTypeNameException(command.name());
        }

        ContainerType containerType = ContainerType.create(command.name());
        return ContainerTypeResultMapper.toResult(containerTypeRepository.save(containerType));
    }
}
