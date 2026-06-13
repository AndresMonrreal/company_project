package com.example.company.container_types.domain.port.in;

import java.util.List;

public interface GetContainerTypeUseCase {

    List<ContainerTypeResult> findAllActive();

    ContainerTypeResult findById(Long id);
}
