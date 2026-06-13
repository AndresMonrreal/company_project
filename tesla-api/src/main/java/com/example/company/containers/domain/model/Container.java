package com.example.company.containers.domain.model;

public final class Container {

    private final Long id;
    private Long containerTypeId;
    private String code;
    private boolean active;

    private Container(Long id, Long containerTypeId, String code, boolean active) {
        this.id = id;
        this.containerTypeId = requireId(containerTypeId, "Container type id is required");
        this.code = requireText(code, "Container code is required");
        this.active = active;
    }

    public static Container create(Long containerTypeId, String code) {
        return new Container(null, containerTypeId, code, true);
    }

    public static Container restore(Long id, Long containerTypeId, String code, boolean active) {
        return new Container(id, containerTypeId, code, active);
    }

    public void update(Long containerTypeId, String code) {
        this.containerTypeId = requireId(containerTypeId, "Container type id is required");
        this.code = requireText(code, "Container code is required");
    }

    public void deactivate() {
        this.active = false;
    }

    public Long id() {
        return id;
    }

    public Long containerTypeId() {
        return containerTypeId;
    }

    public String code() {
        return code;
    }

    public boolean active() {
        return active;
    }

    private static Long requireId(Long value, String message) {
        if (value == null) {
            throw new IllegalArgumentException(message);
        }
        return value;
    }

    private static String requireText(String value, String message) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(message);
        }
        return value.trim();
    }
}
