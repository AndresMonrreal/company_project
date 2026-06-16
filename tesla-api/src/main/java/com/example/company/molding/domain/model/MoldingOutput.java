package com.example.company.molding.domain.model;

import java.time.LocalDateTime;

public final class MoldingOutput {

    private final Long id;
    private final Long cuttingRecordId;
    private final int quantitySent;
    private final Long operatorId;
    private final LocalDateTime sentAt;

    private MoldingOutput(Long id, Long cuttingRecordId, int quantitySent, Long operatorId,
                          LocalDateTime sentAt) {
        this.id = id;
        this.cuttingRecordId = cuttingRecordId;
        this.quantitySent = quantitySent;
        this.operatorId = operatorId;
        this.sentAt = sentAt;
    }

    public static MoldingOutput create(Long cuttingRecordId, int quantitySent, Long operatorId) {
        return new MoldingOutput(null, cuttingRecordId, quantitySent, operatorId, null);
    }

    public static MoldingOutput restore(Long id, Long cuttingRecordId, int quantitySent, Long operatorId,
                                        LocalDateTime sentAt) {
        return new MoldingOutput(id, cuttingRecordId, quantitySent, operatorId, sentAt);
    }

    public Long id() { return id; }
    public Long cuttingRecordId() { return cuttingRecordId; }
    public int quantitySent() { return quantitySent; }
    public Long operatorId() { return operatorId; }
    public LocalDateTime sentAt() { return sentAt; }
}