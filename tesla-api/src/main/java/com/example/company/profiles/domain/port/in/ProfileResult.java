package com.example.company.profiles.domain.port.in;

public record ProfileResult(
        Long id,
        String code,
        String name,
        String description,
        boolean active,
        String type,
        String position
) {
}
