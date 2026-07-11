package com.example.company.profiles.domain.port.out;

import java.util.List;
import java.util.Optional;

import com.example.company.profiles.domain.model.Profile;

public interface ProfileRepositoryPort {

    List<Profile> findAllActiveOrderByCodeAsc();

    Optional<Profile> findActiveById(Long id);

    Optional<Profile> findById(Long id);

    boolean existsByCode(String code);

    boolean hasActiveReceptions(Long profileId);

    Profile save(Profile profile);
}
