package com.example.company.inventory.domain.model;

public final class InventoryItem {

    private final Long id;
    private final Long receptionId;
    private final int availableQuantity;
    private final InventoryItemStatus status;

    private InventoryItem(Long id, Long receptionId, int availableQuantity, InventoryItemStatus status) {
        this.id = id;
        this.receptionId = receptionId;
        this.availableQuantity = availableQuantity;
        this.status = status;
    }

    public static InventoryItem create(Long receptionId, int availableQuantity) {
        return new InventoryItem(null, receptionId, availableQuantity, InventoryItemStatus.AVAILABLE);
    }

    public static InventoryItem restore(Long id, Long receptionId, int availableQuantity, InventoryItemStatus status) {
        return new InventoryItem(id, receptionId, availableQuantity, status);
    }

    public Long id() { return id; }
    public Long receptionId() { return receptionId; }
    public int availableQuantity() { return availableQuantity; }
    public InventoryItemStatus status() { return status; }
}