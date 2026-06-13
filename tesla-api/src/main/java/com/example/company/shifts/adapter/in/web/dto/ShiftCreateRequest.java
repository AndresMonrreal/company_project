package com.example.company.shifts.adapter.in.web.dto;

import java.time.LocalTime;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record ShiftCreateRequest(
        @NotBlank @Size(max = 90) String name,
        @NotNull LocalTime startTime,
        @NotNull LocalTime endTime
) {
}
