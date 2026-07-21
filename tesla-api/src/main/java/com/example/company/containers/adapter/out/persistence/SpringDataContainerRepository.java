package com.example.company.containers.adapter.out.persistence;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface SpringDataContainerRepository extends JpaRepository<ContainerJpaEntity, Long> {

    List<ContainerJpaEntity> findByActiveTrueOrderByCodeAsc();

    Optional<ContainerJpaEntity> findByIdAndActiveTrue(Long id);

    boolean existsByCode(String code);

    boolean existsByCodeAndIdNot(String code, Long id);

    @Query("SELECT COUNT(r) > 0 FROM ReceptionJpaEntity r WHERE r.containerId = :containerId")
    boolean hasActiveReceptions(@Param("containerId") Long containerId);
}
