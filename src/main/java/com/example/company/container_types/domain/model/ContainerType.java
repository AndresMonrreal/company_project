package com.example.company.container_types.domain.model;

public final class ContainerType {

    private final Long id;
    private String name;
    private boolean active;

    private ContainerType(Long id, String name, boolean active) {
        this.id = id;
        this.name = requireText(name, "Container type name is required");
        this.active = active;
    }

    public static ContainerType create(String name) {
        return new ContainerType(null, name, true);
    }

    public static ContainerType restore(Long id, String name, boolean active) {
        return new ContainerType(id, name, active);
    }

    public void update(String name) {
        this.name = requireText(name, "Container type name is required");
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

    private static String requireText(String value, String message) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(message);
        }
        return value.trim();
    }
}
