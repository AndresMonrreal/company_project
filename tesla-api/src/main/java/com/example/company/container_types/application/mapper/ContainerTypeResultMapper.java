package com.example.company.container_types.application.mapper;

import com.example.company.container_types.domain.model.ContainerType;
import com.example.company.container_types.domain.port.in.ContainerTypeResult;

public final class ContainerTypeResultMapper {

    private ContainerTypeResultMapper() {
    }

    public static ContainerTypeResult toResult(ContainerType containerType) {
        return new ContainerTypeResult(
                containerType.id(),
                containerType.name(),
                containerType.active()
        );
    }
}
