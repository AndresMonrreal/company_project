package com.example.company.shifts.adapter.in.web.dto;

import java.time.LocalTime;

public record ShiftResponse(
        Long id,
        String name,
        LocalTime startTime,
        LocalTime endTime,
        boolean active
) {
}
