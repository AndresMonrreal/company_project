package com.example.company.scrap.domain.model;

import java.time.LocalDateTime;

public final class ScrapRecord {

    private final Long id;
    private final Long cuttingRecordId;
    private final int quantity;
    private final String reason;
    private final LocalDateTime createdAt;

    private ScrapRecord(Long id, Long cuttingRecordId, int quantity, String reason,
                        LocalDateTime createdAt) {
        this.id = id;
        this.cuttingRecordId = cuttingRecordId;
        this.quantity = quantity;
        this.reason = reason;
        this.createdAt = createdAt;
    }

    public static ScrapRecord create(Long cuttingRecordId, int quantity, String reason) {
        return new ScrapRecord(null, cuttingRecordId, quantity, reason, null);
    }

    public static ScrapRecord restore(Long id, Long cuttingRecordId, int quantity, String reason,
                                      LocalDateTime createdAt) {
        return new ScrapRecord(id, cuttingRecordId, quantity, reason, createdAt);
    }

    public Long id() { return id; }
    public Long cuttingRecordId() { return cuttingRecordId; }
    public int quantity() { return quantity; }
    public String reason() { return reason; }
    public LocalDateTime createdAt() { return createdAt; }
}