package com.example.company.molding.adapter.out.persistence;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "molding_outputs")
class MoldingOutputJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "cutting_record_id", nullable = false)
    private Long cuttingRecordId;

    @Column(name = "quantity_sent", nullable = false)
    private int quantitySent;

    @Column(name = "operator_id", nullable = false)
    private Long operatorId;

    @Column(name = "sent_at")
    private LocalDateTime sentAt;

    protected MoldingOutputJpaEntity() {
    }

    MoldingOutputJpaEntity(Long id, Long cuttingRecordId, int quantitySent, Long operatorId, LocalDateTime sentAt) {
        this.id = id;
        this.cuttingRecordId = cuttingRecordId;
        this.quantitySent = quantitySent;
        this.operatorId = operatorId;
        this.sentAt = sentAt;
    }

    Long getId() {
        return id;
    }

    Long getCuttingRecordId() {
        return cuttingRecordId;
    }

    int getQuantitySent() {
        return quantitySent;
    }

    Long getOperatorId() {
        return operatorId;
    }

    LocalDateTime getSentAt() {
        return sentAt;
    }
}
