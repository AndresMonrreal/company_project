package com.example.company.profiles.domain.model;

public final class Profile {

    private final Long id;
    private final String code;
    private String name;
    private String description;
    private boolean active;
    private String type;
    private String position;

    private Profile(Long id, String code, String name, String description, boolean active,
                    String type, String position) {
        this.id = id;
        this.code = requireText(code, "Profile code is required");
        this.name = requireText(name, "Profile name is required");
        this.description = normalize(description);
        this.active = active;
        this.type = type;
        this.position = position;
    }

    public static Profile create(String code, String name, String description,
                                 String type, String position) {
        return new Profile(null, code, name, description, true, type, position);
    }

    public static Profile restore(Long id, String code, String name, String description,
                                  boolean active, String type, String position) {
        return new Profile(id, code, name, description, active, type, position);
    }

    public void update(String name, String description, String type, String position) {
        this.name = requireText(name, "Profile name is required");
        this.description = normalize(description);
        this.type = type;
        this.position = position;
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

    public String type() {
        return type;
    }

    public String position() {
        return position;
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
