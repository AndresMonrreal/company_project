package com.example.company.profiles.domain.port.in;

public record UpdateProfileCommand(
        String name,
        String description
) {
}
