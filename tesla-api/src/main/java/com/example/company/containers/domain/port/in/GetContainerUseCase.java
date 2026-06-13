package com.example.company.containers.domain.port.in;

import java.util.List;

public interface GetContainerUseCase {

    List<ContainerResult> findAllActive();

    ContainerResult findById(Long id);
}
