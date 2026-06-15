package com.example.company.activity.domain.port.out;

import java.time.LocalTime;
import java.util.Optional;

public interface ShiftWindowPort {

    Optional<ShiftWindow> findWindowByShiftId(Long shiftId);

    record ShiftWindow(LocalTime startTime, LocalTime endTime) {
    }
}
