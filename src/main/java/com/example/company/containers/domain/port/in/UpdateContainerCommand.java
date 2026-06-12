package com.example.company.containers.domain.port.in;

public record UpdateContainerCommand(
        Long containerTypeId,
        String code
) {
}
