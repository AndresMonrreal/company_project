package com.example.company.molding.domain.model;

import java.time.LocalDateTime;

public final class MoldingOutput {

    private final Long id;
    private final Long cuttingRecordId;
    private final Long operatorId;
    private final int quantitySent;
    private final LocalDateTime sentAt;

    private MoldingOutput(Long id, Long cuttingRecordId, Long operatorId, int quantitySent, LocalDateTime sentAt) {
        this.id = id;
        this.cuttingRecordId = requireId(cuttingRecordId, "cuttingRecordId is required");
        this.operatorId = requireId(operatorId, "operatorId is required");
        this.quantitySent = requireNonNegative(quantitySent, "quantitySent must be >= 0");
        this.sentAt = requireNonNull(sentAt, "sentAt is required");
    }

    public static MoldingOutput create(Long cuttingRecordId, Long operatorId, int quantitySent) {
        return new MoldingOutput(null, cuttingRecordId, operatorId, quantitySent, LocalDateTime.now());
    }

    public static MoldingOutput restore(Long id, Long cuttingRecordId, Long operatorId, int quantitySent, LocalDateTime sentAt) {
        return new MoldingOutput(id, cuttingRecordId, operatorId, quantitySent, sentAt);
    }

    public Long id() {
        return id;
    }

    public Long cuttingRecordId() {
        return cuttingRecordId;
    }

    public Long operatorId() {
        return operatorId;
    }

    public int quantitySent() {
        return quantitySent;
    }

    public LocalDateTime sentAt() {
        return sentAt;
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

    private static LocalDateTime requireNonNull(LocalDateTime value, String message) {
        if (value == null) {
            throw new IllegalArgumentException(message);
        }
        return value;
    }
}
