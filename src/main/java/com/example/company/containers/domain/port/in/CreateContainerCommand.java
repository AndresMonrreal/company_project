package com.example.company.containers.domain.port.in;

public record CreateContainerCommand(
        Long containerTypeId,
        String code
) {
}
