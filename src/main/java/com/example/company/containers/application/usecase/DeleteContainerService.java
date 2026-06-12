package com.example.company.containers.application.usecase;

import com.example.company.containers.domain.exception.ContainerNotFoundException;
import com.example.company.containers.domain.model.Container;
import com.example.company.containers.domain.port.in.DeleteContainerUseCase;
import com.example.company.containers.domain.port.out.ContainerRepositoryPort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class DeleteContainerService implements DeleteContainerUseCase {

    private final ContainerRepositoryPort containerRepository;

    public DeleteContainerService(ContainerRepositoryPort containerRepository) {
        this.containerRepository = containerRepository;
    }

    @Override
    @Transactional
    public void delete(Long id) {
        Container container = containerRepository.findActiveById(id)
                .orElseThrow(() -> new ContainerNotFoundException(id));

        container.deactivate();
        containerRepository.save(container);
    }
}
