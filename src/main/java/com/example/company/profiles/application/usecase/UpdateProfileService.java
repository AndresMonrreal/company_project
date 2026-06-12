package com.example.company.profiles.application.usecase;

import com.example.company.profiles.application.mapper.ProfileResultMapper;
import com.example.company.profiles.domain.exception.ProfileNotFoundException;
import com.example.company.profiles.domain.model.Profile;
import com.example.company.profiles.domain.port.in.ProfileResult;
import com.example.company.profiles.domain.port.in.UpdateProfileCommand;
import com.example.company.profiles.domain.port.in.UpdateProfileUseCase;
import com.example.company.profiles.domain.port.out.ProfileRepositoryPort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UpdateProfileService implements UpdateProfileUseCase {

    private final ProfileRepositoryPort profileRepository;

    public UpdateProfileService(ProfileRepositoryPort profileRepository) {
        this.profileRepository = profileRepository;
    }

    @Override
    @Transactional
    public ProfileResult update(Long id, UpdateProfileCommand command) {
        Profile profile = profileRepository.findActiveById(id)
                .orElseThrow(() -> new ProfileNotFoundException(id));

        profile.update(command.name(), command.description());
        return ProfileResultMapper.toResult(profileRepository.save(profile));
    }
}
