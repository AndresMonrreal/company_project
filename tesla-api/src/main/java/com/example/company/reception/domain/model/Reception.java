package com.example.company.reception.domain.model;

import java.time.LocalDateTime;

public final class Reception {

    private final Long id;
    private final Long containerId;
    private final Long profileId;
    private final Long operatorId;
    private final String lot;
    private final int receivedQuantity;
    private final ReceptionStatus status;
    private final LocalDateTime receivedAt;

    private Reception(Long id, Long containerId, Long profileId, Long operatorId,
                      String lot, int receivedQuantity, ReceptionStatus status, LocalDateTime receivedAt) {
        this.id = id;
        this.containerId = containerId;
        this.profileId = profileId;
        this.operatorId = operatorId;
        this.lot = lot;
        this.receivedQuantity = receivedQuantity;
        this.status = status;
        this.receivedAt = receivedAt;
    }

    public static Reception create(Long containerId, Long profileId, Long operatorId,
                                   String lot, int receivedQuantity) {
        return new Reception(null, containerId, profileId, operatorId,
                lot, receivedQuantity, ReceptionStatus.RECEIVED, null);
    }

    public static Reception restore(Long id, Long containerId, Long profileId, Long operatorId,
                                    String lot, int receivedQuantity, ReceptionStatus status,
                                    LocalDateTime receivedAt) {
        return new Reception(id, containerId, profileId, operatorId,
                lot, receivedQuantity, status, receivedAt);
    }

    public Long id() { return id; }
    public Long containerId() { return containerId; }
    public Long profileId() { return profileId; }
    public Long operatorId() { return operatorId; }
    public String lot() { return lot; }
    public int receivedQuantity() { return receivedQuantity; }
    public ReceptionStatus status() { return status; }
    public LocalDateTime receivedAt() { return receivedAt; }
}