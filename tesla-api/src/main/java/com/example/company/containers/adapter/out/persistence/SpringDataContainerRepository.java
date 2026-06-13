package com.example.company.containers.adapter.out.persistence;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface SpringDataContainerRepository extends JpaRepository<ContainerJpaEntity, Long> {

    List<ContainerJpaEntity> findByActiveTrueOrderByCodeAsc();

    Optional<ContainerJpaEntity> findByIdAndActiveTrue(Long id);

    boolean existsByCode(String code);

    boolean existsByCodeAndIdNot(String code, Long id);
}
