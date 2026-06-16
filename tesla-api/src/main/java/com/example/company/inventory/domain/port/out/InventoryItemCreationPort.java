package com.example.company.inventory.domain.port.out;

public interface InventoryItemCreationPort {

    void createInventoryItem(Long receptionId, int availableQuantity);
}