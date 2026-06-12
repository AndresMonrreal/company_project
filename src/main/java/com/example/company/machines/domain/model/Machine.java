package com.example.company.machines.domain.model;

public final class Machine {

    private static final int MAX_NAME_LENGTH = 80;

    private final Long id;
    private String name;
    private boolean active;

    private Machine(Long id, String name, boolean active) {
        this.id = id;
        this.name = requireText(name, "Machine name is required", MAX_NAME_LENGTH, "Machine name must be 80 characters or fewer");
        this.active = active;
    }

    public static Machine create(String name) {
        return new Machine(null, name, true);
    }

    public static Machine restore(Long id, String name, boolean active) {
        return new Machine(id, name, active);
    }

    public void update(String name) {
        this.name = requireText(name, "Machine name is required", MAX_NAME_LENGTH, "Machine name must be 80 characters or fewer");
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
}
