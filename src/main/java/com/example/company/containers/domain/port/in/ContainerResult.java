package com.example.company.containers.domain.port.in;

public record ContainerResult(
        Long id,
        Long containerTypeId,
        String code,
        boolean active
) {
}
