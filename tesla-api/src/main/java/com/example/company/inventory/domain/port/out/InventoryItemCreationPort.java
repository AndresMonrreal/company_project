package com.example.company.inventory.domain.port.out;

import com.example.company.inventory.domain.model.InventoryItem;

public interface InventoryItemCreationPort {

    InventoryItem create(Long receptionId, int availableQuantity);
}
