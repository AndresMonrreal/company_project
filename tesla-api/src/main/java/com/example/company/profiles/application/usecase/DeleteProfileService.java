package com.example.company.profiles.application.usecase;

import com.example.company.profiles.domain.exception.ProfileNotFoundException;
import com.example.company.profiles.domain.model.Profile;
import com.example.company.profiles.domain.port.in.DeleteProfileUseCase;
import com.example.company.profiles.domain.port.out.ProfileRepositoryPort;
import com.example.company.shared.domain.exception.DomainException;
import com.example.company.shared.domain.exception.DomainErrorType;
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

        if (profileRepository.hasActiveReceptions(id)) {
            throw new DomainException(DomainErrorType.CONFLICT, "profile.has-receptions", "Cannot delete profile because it has associated receptions.") {};
        }

        profile.deactivate();
        profileRepository.save(profile);
    }
}
