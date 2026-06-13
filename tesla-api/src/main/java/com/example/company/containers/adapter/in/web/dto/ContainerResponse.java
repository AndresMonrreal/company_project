package com.example.company.containers.adapter.in.web.dto;

public record ContainerResponse(
        Long id,
        Long containerTypeId,
        String code,
        boolean active
) {
}
