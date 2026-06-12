package com.example.company.shifts.domain.port.in;

public interface CreateShiftUseCase {

    ShiftResult create(CreateShiftCommand command);
}
