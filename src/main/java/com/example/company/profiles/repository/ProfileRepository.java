package com.example.company.profiles.repository;
import com.example.company.profiles.entity.Profile;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ProfileRepository extends JpaRepository<Profile, Long> {
    List<Profile> findByActiveTrueOrderByCodeAsc();

    Optional<Profile> findByCode(String code);
}
