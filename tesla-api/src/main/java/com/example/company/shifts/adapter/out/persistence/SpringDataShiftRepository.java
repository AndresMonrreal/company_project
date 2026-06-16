package com.example.company.shifts.adapter.out.persistence;

import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface SpringDataShiftRepository extends JpaRepository<ShiftJpaEntity, Long> {

    List<ShiftJpaEntity> findByActiveTrueOrderByNameAsc();

    Optional<ShiftJpaEntity> findByIdAndActiveTrue(Long id);

    boolean existsByName(String name);

    boolean existsByNameAndIdNot(String name, Long id);

    @Query("SELECT s FROM ShiftJpaEntity s WHERE s.active = true AND " +
           "((s.startTime <= :now AND s.endTime > :now) OR " +
           "(s.startTime > s.endTime AND (s.startTime <= :now OR s.endTime > :now)))")
    Optional<ShiftJpaEntity> findCurrentByTime(@Param("now") LocalTime now);
}
