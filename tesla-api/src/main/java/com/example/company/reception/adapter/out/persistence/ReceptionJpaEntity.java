package com.example.company.reception.adapter.out.persistence;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;

@Entity
@Table(name = "receptions")
class ReceptionJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "container_id", nullable = false)
    private Long containerId;

    @Column(name = "profile_id", nullable = false)
    private Long profileId;

    @Column(name = "operator_id", nullable = false)
    private Long operatorId;

    @Column(name = "lot", nullable = false, length = 80)
    private String lot;

    @Column(name = "received_quantity", nullable = false)
    private int receivedQuantity;

    @Column(name = "status", nullable = false, length = 40)
    private String status;

    @Column(name = "received_at")
    private LocalDateTime receivedAt;

    @Version
    @Column(name = "version")
    private Long version;

    protected ReceptionJpaEntity() {
    }

    ReceptionJpaEntity(Long containerId, Long profileId, Long operatorId,
                       String lot, int receivedQuantity, String status, LocalDateTime receivedAt) {
        this.containerId = containerId;
        this.profileId = profileId;
        this.operatorId = operatorId;
        this.lot = lot;
        this.receivedQuantity = receivedQuantity;
        this.status = status;
        this.receivedAt = receivedAt;
    }

    Long getId() {
        return id;
    }

    Long getContainerId() {
        return containerId;
    }

    Long getProfileId() {
        return profileId;
    }

    Long getOperatorId() {
        return operatorId;
    }

    String getLot() {
        return lot;
    }

    int getReceivedQuantity() {
        return receivedQuantity;
    }

    String getStatus() {
        return status;
    }

    LocalDateTime getReceivedAt() {
        return receivedAt;
    }

    Long getVersion() {
        return version;
    }
}
