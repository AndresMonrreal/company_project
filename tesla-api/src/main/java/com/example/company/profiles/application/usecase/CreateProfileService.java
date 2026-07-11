package com.example.company.profiles.application.usecase;

import com.example.company.profiles.application.mapper.ProfileResultMapper;
import com.example.company.profiles.domain.exception.DuplicateProfileCodeException;
import com.example.company.profiles.domain.model.Profile;
import com.example.company.profiles.domain.port.in.CreateProfileCommand;
import com.example.company.profiles.domain.port.in.CreateProfileUseCase;
import com.example.company.profiles.domain.port.in.ProfileResult;
import com.example.company.profiles.domain.port.out.ProfileRepositoryPort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CreateProfileService implements CreateProfileUseCase {

    private final ProfileRepositoryPort profileRepository;

    public CreateProfileService(ProfileRepositoryPort profileRepository) {
        this.profileRepository = profileRepository;
    }

    @Override
    @Transactional
    public ProfileResult create(CreateProfileCommand command) {
        if (profileRepository.existsByCode(command.code())) {
            throw new DuplicateProfileCodeException(command.code());
        }

        Profile profile = Profile.create(command.code(), command.name(), command.description(),
                command.type(), command.position());
        return ProfileResultMapper.toResult(profileRepository.save(profile));
    }
}
