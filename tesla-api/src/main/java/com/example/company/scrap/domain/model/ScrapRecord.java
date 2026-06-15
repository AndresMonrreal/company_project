package com.example.company.scrap.domain.model;

import java.time.LocalDateTime;

public final class ScrapRecord {

    private final Long id;
    private final Long cuttingRecordId;
    private final int quantity;
    private final String reason;
    private final LocalDateTime createdAt;

    private ScrapRecord(Long id, Long cuttingRecordId, int quantity, String reason, LocalDateTime createdAt) {
        this.id = id;
        this.cuttingRecordId = requireId(cuttingRecordId, "Cutting record id is required");
        this.quantity = requireNonNegative(quantity, "Scrap quantity must not be negative");
        this.reason = reason;
        this.createdAt = createdAt;
    }

    public static ScrapRecord create(Long cuttingRecordId, int quantity, String reason) {
        return new ScrapRecord(null, cuttingRecordId, quantity, reason, LocalDateTime.now());
    }

    public static ScrapRecord restore(Long id, Long cuttingRecordId, int quantity, String reason, LocalDateTime createdAt) {
        return new ScrapRecord(id, cuttingRecordId, quantity, reason, createdAt);
    }

    public Long id() {
        return id;
    }

    public Long cuttingRecordId() {
        return cuttingRecordId;
    }

    public int quantity() {
        return quantity;
    }

    public String reason() {
        return reason;
    }

    public LocalDateTime createdAt() {
        return createdAt;
    }

    private static Long requireId(Long value, String message) {
        if (value == null) {
            throw new IllegalArgumentException(message);
        }
        return value;
    }

    private static int requireNonNegative(int value, String message) {
        if (value < 0) {
            throw new IllegalArgumentException(message);
        }
        return value;
    }
}
