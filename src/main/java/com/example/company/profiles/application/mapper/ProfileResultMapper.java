package com.example.company.profiles.application.mapper;

import com.example.company.profiles.domain.model.Profile;
import com.example.company.profiles.domain.port.in.ProfileResult;

public final class ProfileResultMapper {

    private ProfileResultMapper() {
    }

    public static ProfileResult toResult(Profile profile) {
        return new ProfileResult(
                profile.id(),
                profile.code(),
                profile.name(),
                profile.description(),
                profile.active()
        );
    }
}
