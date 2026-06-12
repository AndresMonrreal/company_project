package com.example.company.profiles.domain.port.out;

import java.util.List;
import java.util.Optional;

import com.example.company.profiles.domain.model.Profile;

public interface ProfileRepositoryPort {

    List<Profile> findAllActiveOrderByCodeAsc();

    Optional<Profile> findActiveById(Long id);

    boolean existsByCode(String code);

    Profile save(Profile profile);
}
