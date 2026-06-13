package com.example.company.container_types.domain.port.in;

public interface UpdateContainerTypeUseCase {

    ContainerTypeResult update(Long id, UpdateContainerTypeCommand command);
}
