package com.example.company.shifts.application.mapper;

import com.example.company.shifts.domain.model.Shift;
import com.example.company.shifts.domain.port.in.ShiftResult;

public final class ShiftResultMapper {

    private ShiftResultMapper() {
    }

    public static ShiftResult toResult(Shift shift) {
        return new ShiftResult(
                shift.id(),
                shift.name(),
                shift.startTime(),
                shift.endTime(),
                shift.active()
        );
    }
}
