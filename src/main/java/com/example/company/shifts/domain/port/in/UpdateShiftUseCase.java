package com.example.company.shifts.domain.port.in;

public interface UpdateShiftUseCase {

    ShiftResult update(Long id, UpdateShiftCommand command);
}
