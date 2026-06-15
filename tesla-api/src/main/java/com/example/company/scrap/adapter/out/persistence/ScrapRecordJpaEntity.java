package com.example.company.scrap.adapter.out.persistence;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "scrap_records")
class ScrapRecordJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "cutting_record_id", nullable = false)
    private Long cuttingRecordId;

    @Column(name = "quantity", nullable = false)
    private int quantity;

    @Column(name = "reason")
    private String reason;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    protected ScrapRecordJpaEntity() {
    }

    Long getId() {
        return id;
    }

    Long getCuttingRecordId() {
        return cuttingRecordId;
    }

    int getQuantity() {
        return quantity;
    }

    String getReason() {
        return reason;
    }

    LocalDateTime getCreatedAt() {
        return createdAt;
    }

    void setCuttingRecordId(Long cuttingRecordId) {
        this.cuttingRecordId = cuttingRecordId;
    }

    void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    void setReason(String reason) {
        this.reason = reason;
    }

    void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
