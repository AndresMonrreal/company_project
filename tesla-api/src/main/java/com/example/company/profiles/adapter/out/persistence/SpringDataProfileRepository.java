package com.example.company.profiles.adapter.out.persistence;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface SpringDataProfileRepository extends JpaRepository<ProfileJpaEntity, Long> {

    List<ProfileJpaEntity> findByActiveTrueOrderByCodeAsc();

    Optional<ProfileJpaEntity> findByIdAndActiveTrue(Long id);

    boolean existsByCode(String code);
}
