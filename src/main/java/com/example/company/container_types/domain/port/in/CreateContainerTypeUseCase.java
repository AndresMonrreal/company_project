package com.example.company.container_types.domain.port.in;

public interface CreateContainerTypeUseCase {

    ContainerTypeResult create(CreateContainerTypeCommand command);
}
