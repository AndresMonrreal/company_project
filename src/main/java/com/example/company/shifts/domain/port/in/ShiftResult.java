package com.example.company.shifts.domain.port.in;

import java.time.LocalTime;

public record ShiftResult(
        Long id,
        String name,
        LocalTime startTime,
        LocalTime endTime,
        boolean active
) {
}
