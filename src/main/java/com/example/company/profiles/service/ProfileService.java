package com.example.company.profiles.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.company.profiles.dto.ProfileResponse;
import com.example.company.profiles.exception.ProfileNotFoundException;
import com.example.company.profiles.mapper.ProfileMapper;
import com.example.company.profiles.repository.ProfileRepository;
@Service
@Transactional(readOnly = true)
public class ProfileService {

    private final ProfileRepository profileRepository;

    public ProfileService(ProfileRepository profileRepository) {
        this.profileRepository = profileRepository;
    }

    public List<ProfileResponse> findAllActive() {
        return profileRepository.findByActiveTrueOrderByCodeAsc()
                .stream().map(ProfileMapper::toResponse)
                .toList();
    }

    public ProfileResponse findById(Long id) {
        return profileRepository.findById(id)
                .map(ProfileMapper::toResponse)
                .orElseThrow(() -> new ProfileNotFoundException(id));
    }


}
