package com.example.company.profiles.domain.port.in;

public interface CreateProfileUseCase {

    ProfileResult create(CreateProfileCommand command);
}
