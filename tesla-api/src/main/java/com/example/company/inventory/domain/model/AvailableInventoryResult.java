package com.example.company.inventory.domain.model;

public record AvailableInventoryResult(
        Long inventoryItemId,
        String containerCode,
        String profileCode,
        String lot,
        int availableQuantity
) {
}