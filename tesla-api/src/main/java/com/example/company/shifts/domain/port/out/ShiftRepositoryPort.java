package com.example.company.shifts.domain.port.out;

import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import com.example.company.shifts.domain.model.Shift;

public interface ShiftRepositoryPort {

    List<Shift> findAllActiveOrderByNameAsc();

    Optional<Shift> findActiveById(Long id);

    boolean existsByName(String name);

    boolean existsByNameAndIdNot(String name, Long id);

    Optional<Shift> findCurrentShift(LocalTime currentTime);

    Shift save(Shift shift);
}
