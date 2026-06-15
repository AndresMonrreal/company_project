package com.example.company.cutting.adapter.out.persistence;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;

@Entity
@Table(name = "cutting_records")
class CuttingRecordJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "inventory_item_id", nullable = false)
    private Long inventoryItemId;

    @Column(name = "machine_Id", nullable = false)
    private Long machineId;

    @Column(name = "operator_id", nullable = false)
    private Long operatorId;

    @Column(name = "shift_id", nullable = false)
    private Long shiftId;

    @Column(name = "initial_quantity", nullable = false)
    private int initialQuantity;

    @Column(name = "good_quantity", nullable = false)
    private int goodQuantity;

    @Column(name = "scrap_quantity", nullable = false)
    private int scrapQuantity;

    @Column(name = "cut_at")
    private LocalDateTime cutAt;

    @Version
    @Column(name = "version")
    private Long version;

    protected CuttingRecordJpaEntity() {
    }

    CuttingRecordJpaEntity(Long inventoryItemId, Long machineId, Long operatorId,
                           Long shiftId, int initialQuantity, int goodQuantity,
                           int scrapQuantity, LocalDateTime cutAt) {
        this.inventoryItemId = inventoryItemId;
        this.machineId = machineId;
        this.operatorId = operatorId;
        this.shiftId = shiftId;
        this.initialQuantity = initialQuantity;
        this.goodQuantity = goodQuantity;
        this.scrapQuantity = scrapQuantity;
        this.cutAt = cutAt;
    }

    Long getId() {
        return id;
    }

    Long getInventoryItemId() {
        return inventoryItemId;
    }

    Long getMachineId() {
        return machineId;
    }

    Long getOperatorId() {
        return operatorId;
    }

    Long getShiftId() {
        return shiftId;
    }

    int getInitialQuantity() {
        return initialQuantity;
    }

    int getGoodQuantity() {
        return goodQuantity;
    }

    int getScrapQuantity() {
        return scrapQuantity;
    }

    LocalDateTime getCutAt() {
        return cutAt;
    }
}
