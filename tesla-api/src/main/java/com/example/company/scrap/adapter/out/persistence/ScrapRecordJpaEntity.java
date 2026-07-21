package com.example.company.scrap.adapter.out.persistence;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

@Entity
@Table(name = "scrap_records")
public class ScrapRecordJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "shift_id", nullable = false)
    private Long shiftId;

    @Column(name = "profile_id", nullable = false)
    private Long profileId;

    @Column(name = "operator_id", nullable = false)
    private Long operatorId;

    @Column(nullable = false)
    private int quantity;

    @Column(length = 255)
    private String reason;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    protected ScrapRecordJpaEntity() {
    }

    ScrapRecordJpaEntity(Long shiftId, Long profileId, Long operatorId, int quantity, String reason) {
        this.shiftId = shiftId;
        this.profileId = profileId;
        this.operatorId = operatorId;
        this.quantity = quantity;
        this.reason = reason;
    }

    @PrePersist
    void onCreate() {
        createdAt = LocalDateTime.now();
    }

    Long getId() { return id; }
    Long getShiftId() { return shiftId; }
    Long getProfileId() { return profileId; }
    Long getOperatorId() { return operatorId; }
    int getQuantity() { return quantity; }
    String getReason() { return reason; }
    LocalDateTime getCreatedAt() { return createdAt; }
}