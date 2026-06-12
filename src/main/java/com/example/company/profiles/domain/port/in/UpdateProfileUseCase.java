package com.example.company.profiles.domain.port.in;

public interface UpdateProfileUseCase {

    ProfileResult update(Long id, UpdateProfileCommand command);
}
