package com.example.company.cutting.domain.port.in;

public record RegisterCutCommand(
        Long inventoryItemId,
        Long machineId,
        Long operatorId,
        Long shiftId,
        int initialQuantity,
        int goodQuantity,
        int scrapQuantity
) {
}
