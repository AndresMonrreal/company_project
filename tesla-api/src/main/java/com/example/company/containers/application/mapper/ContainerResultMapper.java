package com.example.company.containers.application.mapper;

import com.example.company.containers.domain.model.Container;
import com.example.company.containers.domain.port.in.ContainerResult;

public final class ContainerResultMapper {

    private ContainerResultMapper() {
    }

    public static ContainerResult toResult(Container container) {
        return new ContainerResult(
                container.id(),
                container.containerTypeId(),
                container.code(),
                container.active()
        );
    }
}
