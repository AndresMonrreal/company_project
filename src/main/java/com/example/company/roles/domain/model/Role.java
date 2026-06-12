package com.example.company.roles.domain.model;

public final class Role {

    private static final int MAX_NAME_LENGTH = 80;
    private static final int MAX_DESCRIPTION_LENGTH = 255;

    private final Long id;
    private String name;
    private String description;
    private boolean active;

    private Role(Long id, String name, String description, boolean active) {
        this.id = id;
        this.name = requireText(name, "Role name is required", MAX_NAME_LENGTH, "Role name must be 80 characters or fewer");
        this.description = optionalText(description, MAX_DESCRIPTION_LENGTH, "Role description must be 255 characters or fewer");
        this.active = active;
    }

    public static Role create(String name, String description) {
        return new Role(null, name, description, true);
    }

    public static Role restore(Long id, String name, String description, boolean active) {
        return new Role(id, name, description, active);
    }

    public void update(String name, String description) {
        this.name = requireText(name, "Role name is required", MAX_NAME_LENGTH, "Role name must be 80 characters or fewer");
        this.description = optionalText(description, MAX_DESCRIPTION_LENGTH, "Role description must be 255 characters or fewer");
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

    public String description() {
        return description;
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

    private static String optionalText(String value, int maxLength, String lengthMessage) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        if (trimmed.length() > maxLength) {
            throw new IllegalArgumentException(lengthMessage);
        }
        return trimmed;
    }
}
