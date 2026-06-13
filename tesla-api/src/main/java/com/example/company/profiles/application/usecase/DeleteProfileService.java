package com.example.company.profiles.application.usecase;

import com.example.company.profiles.domain.exception.ProfileNotFoundException;
import com.example.company.profiles.domain.model.Profile;
import com.example.company.profiles.domain.port.in.DeleteProfileUseCase;
import com.example.company.profiles.domain.port.out.ProfileRepositoryPort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class DeleteProfileService implements DeleteProfileUseCase {

    private final ProfileRepositoryPort profileRepository;

    public DeleteProfileService(ProfileRepositoryPort profileRepository) {
        this.profileRepository = profileRepository;
    }

    @Override
    @Transactional
    public void delete(Long id) {
        Profile profile = profileRepository.findActiveById(id)
                .orElseThrow(() -> new ProfileNotFoundException(id));

        profile.deactivate();
        profileRepository.save(profile);
    }
}
