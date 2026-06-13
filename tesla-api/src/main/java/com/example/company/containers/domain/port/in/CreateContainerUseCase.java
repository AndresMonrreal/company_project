package com.example.company.containers.domain.port.in;

public interface CreateContainerUseCase {

    ContainerResult create(CreateContainerCommand command);
}
