package com.example.company.profiles.adapter.out.persistence;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface SpringDataProfileRepository extends JpaRepository<ProfileJpaEntity, Long> {

    List<ProfileJpaEntity> findByActiveTrueOrderByCodeAsc();

    Optional<ProfileJpaEntity> findByIdAndActiveTrue(Long id);

    boolean existsByCode(String code);

    @Query("SELECT COUNT(r) > 0 FROM ReceptionJpaEntity r WHERE r.profileId = :profileId")
    boolean hasActiveReceptions(@Param("profileId") Long profileId);
}
