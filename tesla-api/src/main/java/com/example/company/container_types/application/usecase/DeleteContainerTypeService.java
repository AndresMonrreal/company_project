package com.example.company.container_types.application.usecase;

import com.example.company.container_types.domain.exception.ContainerTypeNotFoundException;
import com.example.company.container_types.domain.model.ContainerType;
import com.example.company.container_types.domain.port.in.DeleteContainerTypeUseCase;
import com.example.company.container_types.domain.port.out.ContainerTypeRepositoryPort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class DeleteContainerTypeService implements DeleteContainerTypeUseCase {

    private final ContainerTypeRepositoryPort containerTypeRepository;

    public DeleteContainerTypeService(ContainerTypeRepositoryPort containerTypeRepository) {
        this.containerTypeRepository = containerTypeRepository;
    }

    @Override
    @Transactional
    public void delete(Long id) {
        ContainerType containerType = containerTypeRepository.findActiveById(id)
                .orElseThrow(() -> new ContainerTypeNotFoundException(id));

        containerType.deactivate();
        containerTypeRepository.save(containerType);
    }
}
