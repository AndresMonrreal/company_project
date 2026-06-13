package com.example.company.roles.adapter.out.persistence;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface SpringDataRoleRepository extends JpaRepository<RoleJpaEntity, Long> {

    List<RoleJpaEntity> findByActiveTrueOrderByNameAsc();

    Optional<RoleJpaEntity> findByIdAndActiveTrue(Long id);

    boolean existsByName(String name);

    boolean existsByNameAndIdNot(String name, Long id);
}
