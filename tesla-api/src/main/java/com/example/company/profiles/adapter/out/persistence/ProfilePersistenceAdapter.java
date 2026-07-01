package com.example.company.profiles.adapter.out.persistence;

import java.util.List;
import java.util.Optional;

import com.example.company.profiles.domain.model.Profile;
import com.example.company.profiles.domain.port.out.ProfileRepositoryPort;
import org.springframework.stereotype.Repository;

@Repository
public class ProfilePersistenceAdapter implements ProfileRepositoryPort {

    private final SpringDataProfileRepository profileRepository;
    private final ProfilePersistenceMapper mapper;

    public ProfilePersistenceAdapter(SpringDataProfileRepository profileRepository, ProfilePersistenceMapper mapper) {
        this.profileRepository = profileRepository;
        this.mapper = mapper;
    }

    @Override
    public List<Profile> findAllActiveOrderByCodeAsc() {
        return profileRepository.findByActiveTrueOrderByCodeAsc()
                .stream()
                .map(mapper::toDomain)
                .toList();
    }

    @Override
    public Optional<Profile> findActiveById(Long id) {
        return profileRepository.findByIdAndActiveTrue(id).map(mapper::toDomain);
    }

    @Override
    public boolean existsByCode(String code) {
        return profileRepository.existsByCode(code);
    }

    @Override
    public boolean hasActiveReceptions(Long profileId) {
        return profileRepository.hasActiveReceptions(profileId);
    }

    @Override
    public Profile save(Profile profile) {
        ProfileJpaEntity entity = profile.id() == null
                ? mapper.toNewEntity(profile)
                : profileRepository.findById(profile.id())
                        .orElseThrow(() -> new IllegalStateException("Profile entity disappeared during save: " + profile.id()));

        entity.updateFromDomain(profile.code(), profile.name(), profile.description(), profile.active());
        return mapper.toDomain(profileRepository.save(entity));
    }
}
