package com.example.company.scrap.domain.model;

import java.time.LocalDateTime;

public final class ScrapRecord {

    private final Long id;
    private final Long shiftId;
    private final Long profileId;
    private final Long operatorId;
    private final int quantity;
    private final String reason;
    private final LocalDateTime createdAt;

    private ScrapRecord(Long id, Long shiftId, Long profileId, Long operatorId,
                        int quantity, String reason, LocalDateTime createdAt) {
        this.id = id;
        this.shiftId = shiftId;
        this.profileId = profileId;
        this.operatorId = operatorId;
        this.quantity = quantity;
        this.reason = reason;
        this.createdAt = createdAt;
    }

    public static ScrapRecord create(Long shiftId, Long profileId, Long operatorId,
                                     int quantity, String reason) {
        return new ScrapRecord(null, shiftId, profileId, operatorId, quantity, reason, null);
    }

    public static ScrapRecord restore(Long id, Long shiftId, Long profileId, Long operatorId,
                                      int quantity, String reason, LocalDateTime createdAt) {
        return new ScrapRecord(id, shiftId, profileId, operatorId, quantity, reason, createdAt);
    }

    public Long id() { return id; }
    public Long shiftId() { return shiftId; }
    public Long profileId() { return profileId; }
    public Long operatorId() { return operatorId; }
    public int quantity() { return quantity; }
    public String reason() { return reason; }
    public LocalDateTime createdAt() { return createdAt; }
}