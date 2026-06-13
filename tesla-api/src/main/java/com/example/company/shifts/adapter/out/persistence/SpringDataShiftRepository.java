package com.example.company.shifts.adapter.out.persistence;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface SpringDataShiftRepository extends JpaRepository<ShiftJpaEntity, Long> {

    List<ShiftJpaEntity> findByActiveTrueOrderByNameAsc();

    Optional<ShiftJpaEntity> findByIdAndActiveTrue(Long id);

    boolean existsByName(String name);

    boolean existsByNameAndIdNot(String name, Long id);
}
