package com.example.company.profiles.application.usecase;

import java.util.List;

import com.example.company.profiles.application.mapper.ProfileResultMapper;
import com.example.company.profiles.domain.exception.ProfileNotFoundException;
import com.example.company.profiles.domain.port.in.GetProfileUseCase;
import com.example.company.profiles.domain.port.in.ProfileResult;
import com.example.company.profiles.domain.port.out.ProfileRepositoryPort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class GetProfileService implements GetProfileUseCase {

    private final ProfileRepositoryPort profileRepository;

    public GetProfileService(ProfileRepositoryPort profileRepository) {
        this.profileRepository = profileRepository;
    }

    @Override
    public List<ProfileResult> findAllActive() {
        return profileRepository.findAllActiveOrderByCodeAsc()
                .stream()
                .map(ProfileResultMapper::toResult)
                .toList();
    }

    @Override
    public ProfileResult findById(Long id) {
        return profileRepository.findActiveById(id)
                .map(ProfileResultMapper::toResult)
                .orElseThrow(() -> new ProfileNotFoundException(id));
    }
}
