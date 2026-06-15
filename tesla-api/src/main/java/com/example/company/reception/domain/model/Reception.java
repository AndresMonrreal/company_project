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
        this.containerId = requireId(containerId, "container is required");
        this.profileId = requireId(profileId, "profile is required");
        this.operatorId = requireId(operatorId, "operator is required");
        this.lot = requireLot(lot);
        this.receivedQuantity = requirePositive(receivedQuantity);
        this.status = status;
        this.receivedAt = receivedAt;
    }

    public static Reception create(Long containerId, Long profileId, Long operatorId,
                                   String lot, int receivedQuantity) {
        return new Reception(null, containerId, profileId, operatorId,
                lot, receivedQuantity, ReceptionStatus.RECEIVED, LocalDateTime.now());
    }

    public static Reception restore(Long id, Long containerId, Long profileId, Long operatorId,
                                    String lot, int receivedQuantity, ReceptionStatus status,
                                    LocalDateTime receivedAt) {
        return new Reception(id, containerId, profileId, operatorId, lot,
                receivedQuantity, status, receivedAt);
    }

    public Long id() {
        return id;
    }

    public Long containerId() {
        return containerId;
    }

    public Long profileId() {
        return profileId;
    }

    public Long operatorId() {
        return operatorId;
    }

    public String lot() {
        return lot;
    }

    public int receivedQuantity() {
        return receivedQuantity;
    }

    public ReceptionStatus status() {
        return status;
    }

    public LocalDateTime receivedAt() {
        return receivedAt;
    }

    private static Long requireId(Long value, String message) {
        if (value == null) {
            throw new IllegalArgumentException(message);
        }
        return value;
    }

    private static String requireLot(String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("lot is required");
        }
        String trimmed = value.trim();
        if (trimmed.length() > 80) {
            throw new IllegalArgumentException("lot must be 80 characters or fewer");
        }
        return trimmed;
    }

    private static int requirePositive(int value) {
        if (value <= 0) {
            throw new IllegalArgumentException("received_quantity must be greater than zero");
        }
        return value;
    }
}
