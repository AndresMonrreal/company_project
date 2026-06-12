package com.example.company.container_types.adapter.out.persistence;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface SpringDataContainerTypeRepository extends JpaRepository<ContainerTypeJpaEntity, Long> {

    List<ContainerTypeJpaEntity> findByActiveTrueOrderByNameAsc();

    Optional<ContainerTypeJpaEntity> findByIdAndActiveTrue(Long id);

    boolean existsByName(String name);

    boolean existsByNameAndIdNot(String name, Long id);
}
