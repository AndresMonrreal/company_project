package com.example.company.machines.domain.model;

import java.time.LocalDate;

public final class Machine {

    private static final int MAX_NAME_LENGTH = 80;

    private final Long id;
    private String name;
    private boolean active;
    private String code;
    private String status;
    private String processesType;
    private Double cycleTimeSeconds;
    private LocalDate lastMaintenanceDate;
    private String observations;

    private Machine(Long id, String name, boolean active, String code, String status,
                    String processesType, Double cycleTimeSeconds,
                    LocalDate lastMaintenanceDate, String observations) {
        this.id = id;
        this.name = requireText(name, "Machine name is required", MAX_NAME_LENGTH, "Machine name must be 80 characters or fewer");
        this.active = active;
        this.code = code;
        this.status = status;
        this.processesType = processesType;
        this.cycleTimeSeconds = cycleTimeSeconds;
        this.lastMaintenanceDate = lastMaintenanceDate;
        this.observations = observations;
    }

    public static Machine create(String name, String code, String status, String processesType,
                                 Double cycleTimeSeconds, LocalDate lastMaintenanceDate,
                                 String observations) {
        return new Machine(null, name, true, code, status, processesType,
                cycleTimeSeconds, lastMaintenanceDate, observations);
    }

    public static Machine restore(Long id, String name, boolean active, String code, String status,
                                  String processesType, Double cycleTimeSeconds,
                                  LocalDate lastMaintenanceDate, String observations) {
        return new Machine(id, name, active, code, status, processesType,
                cycleTimeSeconds, lastMaintenanceDate, observations);
    }

    public void update(String name, String code, String status, String processesType,
                       Double cycleTimeSeconds, LocalDate lastMaintenanceDate,
                       String observations) {
        this.name = requireText(name, "Machine name is required", MAX_NAME_LENGTH, "Machine name must be 80 characters or fewer");
        this.code = code;
        this.status = status;
        this.processesType = processesType;
        this.cycleTimeSeconds = cycleTimeSeconds;
        this.lastMaintenanceDate = lastMaintenanceDate;
        this.observations = observations;
    }

    public void deactivate() {
        this.active = false;
    }

    public Long id() { return id; }
    public String name() { return name; }
    public boolean active() { return active; }
    public String code() { return code; }
    public String status() { return status; }
    public String processesType() { return processesType; }
    public Double cycleTimeSeconds() { return cycleTimeSeconds; }
    public LocalDate lastMaintenanceDate() { return lastMaintenanceDate; }
    public String observations() { return observations; }

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
}
