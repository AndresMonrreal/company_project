package com.example.company.profiles.domain.port.in;

public record CreateProfileCommand(
        String code,
        String name,
        String description,
        String type,
        String position
) {
}
