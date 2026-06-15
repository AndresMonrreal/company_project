package com.example.company.shifts.domain.port.in;

import java.util.List;

public interface GetShiftUseCase {

    List<ShiftResult> findAllActive();

    ShiftResult findById(Long id);

    ShiftResult findCurrentShift();
}
