package com.example.company.containers.domain.port.in;

public interface UpdateContainerUseCase {

    ContainerResult update(Long id, UpdateContainerCommand command);
}
