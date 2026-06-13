package com.example.company.shifts.domain.model;

import java.time.LocalTime;

public final class Shift {

    private static final int MAX_NAME_LENGTH = 90;

    private final Long id;
    private String name;
    private LocalTime startTime;
    private LocalTime endTime;
    private boolean active;

    private Shift(Long id, String name, LocalTime startTime, LocalTime endTime, boolean active) {
        this.id = id;
        this.name = requireText(name, "Shift name is required", MAX_NAME_LENGTH, "Shift name must be 90 characters or fewer");
        this.startTime = requireTime(startTime, "Shift start time is required");
        this.endTime = requireTime(endTime, "Shift end time is required");
        requireDifferentTimes(this.startTime, this.endTime);
        this.active = active;
    }

    public static Shift create(String name, LocalTime startTime, LocalTime endTime) {
        return new Shift(null, name, startTime, endTime, true);
    }

    public static Shift restore(Long id, String name, LocalTime startTime, LocalTime endTime, boolean active) {
        return new Shift(id, name, startTime, endTime, active);
    }

    public void update(String name, LocalTime startTime, LocalTime endTime) {
        this.name = requireText(name, "Shift name is required", MAX_NAME_LENGTH, "Shift name must be 90 characters or fewer");
        this.startTime = requireTime(startTime, "Shift start time is required");
        this.endTime = requireTime(endTime, "Shift end time is required");
        requireDifferentTimes(this.startTime, this.endTime);
    }

    public void deactivate() {
        this.active = false;
    }

    public Long id() {
        return id;
    }

    public String name() {
        return name;
    }

    public LocalTime startTime() {
        return startTime;
    }

    public LocalTime endTime() {
        return endTime;
    }

    public boolean active() {
        return active;
    }

    private static String requireText(String value, String requiredMessage, int maxLength, String lengthMessage) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(requiredMessage);
        }
        String trimmed = value.trim();
        if (trimmed.length() > maxLength) {
            throw new IllegalArgumentException(lengthMessage);
        }
        return trimmed;
    }

    private static LocalTime requireTime(LocalTime value, String message) {
        if (value == null) {
            throw new IllegalArgumentException(message);
        }
        return value;
    }

    private static void requireDifferentTimes(LocalTime startTime, LocalTime endTime) {
        if (startTime.equals(endTime)) {
            throw new IllegalArgumentException("Shift start time and end time must be different");
        }
    }
}
