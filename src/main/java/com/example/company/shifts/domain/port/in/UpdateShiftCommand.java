package com.example.company.shifts.domain.port.in;

import java.time.LocalTime;

public record UpdateShiftCommand(
        String name,
        LocalTime startTime,
        LocalTime endTime
) {
}
