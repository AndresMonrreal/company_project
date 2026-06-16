package com.example.company.molding.adapter.out.persistence;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

@Entity
@Table(name = "molding_outputs")
public class MoldingOutputJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "cutting_record_id", nullable = false)
    private Long cuttingRecordId;

    @Column(name = "quantity_sent", nullable = false)
    private int quantitySent;

    @Column(name = "operator_id", nullable = false)
    private Long operatorId;

    @Column(name = "sent_at", nullable = false, updatable = false)
    private LocalDateTime sentAt;

    protected MoldingOutputJpaEntity() {
    }

    MoldingOutputJpaEntity(Long cuttingRecordId, int quantitySent, Long operatorId) {
        this.cuttingRecordId = cuttingRecordId;
        this.quantitySent = quantitySent;
        this.operatorId = operatorId;
    }

    @PrePersist
    void onCreate() {
        sentAt = LocalDateTime.now();
    }

    Long getId() { return id; }
    Long getCuttingRecordId() { return cuttingRecordId; }
    int getQuantitySent() { return quantitySent; }
    Long getOperatorId() { return operatorId; }
    LocalDateTime getSentAt() { return sentAt; }
}