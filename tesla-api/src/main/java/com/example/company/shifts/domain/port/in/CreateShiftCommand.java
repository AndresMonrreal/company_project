package com.example.company.shifts.domain.port.in;

import java.time.LocalTime;

public record CreateShiftCommand(
        String name,
        LocalTime startTime,
        LocalTime endTime
) {
}
