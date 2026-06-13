package com.example.company.container_types.application.usecase;

import com.example.company.container_types.application.mapper.ContainerTypeResultMapper;
import com.example.company.container_types.domain.exception.ContainerTypeNotFoundException;
import com.example.company.container_types.domain.exception.DuplicateContainerTypeNameException;
import com.example.company.container_types.domain.model.ContainerType;
import com.example.company.container_types.domain.port.in.ContainerTypeResult;
import com.example.company.container_types.domain.port.in.UpdateContainerTypeCommand;
import com.example.company.container_types.domain.port.in.UpdateContainerTypeUseCase;
import com.example.company.container_types.domain.port.out.ContainerTypeRepositoryPort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UpdateContainerTypeService implements UpdateContainerTypeUseCase {

    private final ContainerTypeRepositoryPort containerTypeRepository;

    public UpdateContainerTypeService(ContainerTypeRepositoryPort containerTypeRepository) {
        this.containerTypeRepository = containerTypeRepository;
    }

    @Override
    @Transactional
    public ContainerTypeResult update(Long id, UpdateContainerTypeCommand command) {
        ContainerType containerType = containerTypeRepository.findActiveById(id)
                .orElseThrow(() -> new ContainerTypeNotFoundException(id));

        if (containerTypeRepository.existsByNameAndIdNot(command.name(), id)) {
            throw new DuplicateContainerTypeNameException(command.name());
        }

        containerType.update(command.name());
        return ContainerTypeResultMapper.toResult(containerTypeRepository.save(containerType));
    }
}
