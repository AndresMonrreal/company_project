package com.example.company.profiles.adapter.in.web;

import com.example.company.profiles.adapter.in.web.dto.ProfileCreateRequest;
import com.example.company.profiles.adapter.in.web.dto.ProfileResponse;
import com.example.company.profiles.adapter.in.web.dto.ProfileUpdateRequest;
import com.example.company.profiles.domain.port.in.CreateProfileCommand;
import com.example.company.profiles.domain.port.in.ProfileResult;
import com.example.company.profiles.domain.port.in.UpdateProfileCommand;
import org.springframework.stereotype.Component;

@Component
public class ProfileWebMapper {

    CreateProfileCommand toCommand(ProfileCreateRequest request) {
        return new CreateProfileCommand(request.code(), request.name(), request.description(),
                request.type(), request.position());
    }

    UpdateProfileCommand toCommand(ProfileUpdateRequest request) {
        return new UpdateProfileCommand(request.name(), request.description(),
                request.type(), request.position());
    }

    ProfileResponse toResponse(ProfileResult result) {
        return new ProfileResponse(
                result.id(),
                result.code(),
                result.name(),
                result.description(),
                result.active(),
                result.type(),
                result.position()
        );
    }
}
