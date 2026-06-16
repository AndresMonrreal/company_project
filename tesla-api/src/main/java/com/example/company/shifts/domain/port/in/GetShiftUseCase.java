package com.example.company.shifts.domain.port.in;

import java.util.List;
import java.util.Optional;

public interface GetShiftUseCase {

    List<ShiftResult> findAllActive();

    ShiftResult findById(Long id);

    Optional<ShiftResult> findCurrent();
}
