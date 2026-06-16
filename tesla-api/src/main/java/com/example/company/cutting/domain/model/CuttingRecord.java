package com.example.company.cutting.domain.model;

import java.time.LocalDateTime;

public final class CuttingRecord {

    private final Long id;
    private final Long inventoryItemId;
    private final Long machineId;
    private final Long operatorId;
    private final Long shiftId;
    private final CuttingQuantities quantities;
    private final LocalDateTime cutAt;

    private CuttingRecord(Long id, Long inventoryItemId, Long machineId, Long operatorId,
                          Long shiftId, CuttingQuantities quantities, LocalDateTime cutAt) {
        this.id = id;
        this.inventoryItemId = inventoryItemId;
        this.machineId = machineId;
        this.operatorId = operatorId;
        this.shiftId = shiftId;
        this.quantities = quantities;
        this.cutAt = cutAt;
    }

    public static CuttingRecord create(Long inventoryItemId, Long machineId, Long operatorId,
                                       Long shiftId, CuttingQuantities quantities) {
        return new CuttingRecord(null, inventoryItemId, machineId, operatorId, shiftId, quantities, null);
    }

    public static CuttingRecord restore(Long id, Long inventoryItemId, Long machineId, Long operatorId,
                                        Long shiftId, CuttingQuantities quantities, LocalDateTime cutAt) {
        return new CuttingRecord(id, inventoryItemId, machineId, operatorId, shiftId, quantities, cutAt);
    }

    public Long id() { return id; }
    public Long inventoryItemId() { return inventoryItemId; }
    public Long machineId() { return machineId; }
    public Long operatorId() { return operatorId; }
    public Long shiftId() { return shiftId; }
    public CuttingQuantities quantities() { return quantities; }
    public LocalDateTime cutAt() { return cutAt; }
}