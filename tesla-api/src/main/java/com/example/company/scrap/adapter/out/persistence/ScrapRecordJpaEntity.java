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

    @Column(name = "cutting_record_id", nullable = false)
    private Long cuttingRecordId;

    @Column(nullable = false)
    private int quantity;

    @Column(length = 255)
    private String reason;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    protected ScrapRecordJpaEntity() {
    }

    ScrapRecordJpaEntity(Long cuttingRecordId, int quantity, String reason) {
        this.cuttingRecordId = cuttingRecordId;
        this.quantity = quantity;
        this.reason = reason;
    }

    @PrePersist
    void onCreate() {
        createdAt = LocalDateTime.now();
    }

    Long getId() { return id; }
    Long getCuttingRecordId() { return cuttingRecordId; }
    int getQuantity() { return quantity; }
    String getReason() { return reason; }
    LocalDateTime getCreatedAt() { return createdAt; }
}