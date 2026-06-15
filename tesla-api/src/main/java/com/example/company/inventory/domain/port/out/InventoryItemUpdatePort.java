package com.example.company.inventory.domain.port.out;

public interface InventoryItemUpdatePort {

    void updateToCut(Long inventoryItemId, int goodQuantity);
}
