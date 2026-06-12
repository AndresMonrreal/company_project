package com.example.company.machines.adapter.out.persistence;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface SpringDataMachineRepository extends JpaRepository<MachineJpaEntity, Long> {

    List<MachineJpaEntity> findByActiveTrueOrderByNameAsc();

    Optional<MachineJpaEntity> findByIdAndActiveTrue(Long id);

    boolean existsByName(String name);

    boolean existsByNameAndIdNot(String name, Long id);
}
