package com.example.company.profiles.mapper;

import com.example.company.profiles.dto.ProfileResponse;
import com.example.company.profiles.entity.Profile;

public final class ProfileMapper {
    private ProfileMapper() {}

    public static ProfileResponse toResponse(Profile profile) {
        return new ProfileResponse(
                profile.getId(),
                profile.getCode(),
                profile.getName(),
                profile.getDescription(),
                profile.isActive()
        );
    }
}
