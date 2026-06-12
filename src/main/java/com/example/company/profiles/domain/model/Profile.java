package com.example.company.profiles.domain.model;

public final class Profile {

    private final Long id;
    private final String code;
    private String name;
    private String description;
    private boolean active;

    private Profile(Long id, String code, String name, String description, boolean active) {
        this.id = id;
        this.code = requireText(code, "Profile code is required");
        this.name = requireText(name, "Profile name is required");
        this.description = normalize(description);
        this.active = active;
    }

    public static Profile create(String code, String name, String description) {
        return new Profile(null, code, name, description, true);
    }

    public static Profile restore(Long id, String code, String name, String description, boolean active) {
        return new Profile(id, code, name, description, active);
    }

    public void update(String name, String description) {
        this.name = requireText(name, "Profile name is required");
        this.description = normalize(description);
    }

    public void deactivate() {
        this.active = false;
    }

    public Long id() {
        return id;
    }

    public String code() {
        return code;
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

    private static String requireText(String value, String message) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(message);
        }
        return value.trim();
    }

    private static String normalize(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}
