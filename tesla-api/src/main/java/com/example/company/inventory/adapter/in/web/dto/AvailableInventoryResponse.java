package com.example.company.inventory.adapter.in.web.dto;

public record AvailableInventoryResponse(
        Long inventoryItemId,
        String containerCode,
        String profileCode,
        String lot,
        int availableQuantity
) {
}