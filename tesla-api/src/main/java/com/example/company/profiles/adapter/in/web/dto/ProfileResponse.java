package com.example.company.profiles.adapter.in.web.dto;

public record ProfileResponse(
        Long id,
        String code,
        String name,
        String description,
        boolean active,
        String type,
        String position
) {
}
