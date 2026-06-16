package com.example.company.inventory.domain.port.out;

public interface InventoryItemUpdatePort {

    void markAsCut(Long inventoryItemId);
}