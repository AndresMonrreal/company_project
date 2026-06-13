package com.example.company.shifts.domain.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalTime;

import org.junit.jupiter.api.Test;

class ShiftTest {

    @Test
    void createsActiveShiftWithTrimmedName() {
        Shift shift = Shift.create("  First Shift  ", LocalTime.of(6, 0), LocalTime.of(14, 0));

        assertThat(shift.id()).isNull();
        assertThat(shift.name()).isEqualTo("First Shift");
        assertThat(shift.startTime()).isEqualTo(LocalTime.of(6, 0));
        assertThat(shift.endTime()).isEqualTo(LocalTime.of(14, 0));
        assertThat(shift.active()).isTrue();
    }

    @Test
    void rejectsBlankName() {
        assertThatThrownBy(() -> Shift.create(" ", LocalTime.of(6, 0), LocalTime.of(14, 0)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Shift name is required");
    }

    @Test
    void rejectsMissingStartTime() {
        assertThatThrownBy(() -> Shift.create("First Shift", null, LocalTime.of(14, 0)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Shift start time is required");
    }

    @Test
    void rejectsMissingEndTime() {
        assertThatThrownBy(() -> Shift.create("First Shift", LocalTime.of(6, 0), null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Shift end time is required");
    }

    @Test
    void rejectsEqualStartAndEndTime() {
        assertThatThrownBy(() -> Shift.create("First Shift", LocalTime.of(6, 0), LocalTime.of(6, 0)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Shift start time and end time must be different");
    }

    @Test
    void allowsOvernightShift() {
        Shift shift = Shift.create("Night Shift", LocalTime.of(22, 0), LocalTime.of(6, 0));

        assertThat(shift.startTime()).isEqualTo(LocalTime.of(22, 0));
        assertThat(shift.endTime()).isEqualTo(LocalTime.of(6, 0));
    }

    @Test
    void softDeletesShift() {
        Shift shift = Shift.restore(1L, "First Shift", LocalTime.of(6, 0), LocalTime.of(14, 0), true);

        shift.deactivate();

        assertThat(shift.active()).isFalse();
    }
}
