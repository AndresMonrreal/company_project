package com.example.company.shifts.domain.port.out;

import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import com.example.company.shifts.domain.model.Shift;

public interface ShiftRepositoryPort {

    List<Shift> findAllActiveOrderByNameAsc();

    Optional<Shift> findActiveById(Long id);

    Optional<Shift> findCurrentByTime(LocalTime now);

    boolean existsByName(String name);

    boolean existsByNameAndIdNot(String name, Long id);

    boolean hasActiveCuttingRecords(Long shiftId);

    Shift save(Shift shift);
}
