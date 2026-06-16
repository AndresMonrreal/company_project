package com.example.company.cutting.domain.port.in;

public record RegisterCuttingCommand(
        Long inventoryItemId,
        Long machineId,
        Long shiftId,
        int initialQuantity,
        int goodQuantity,
        int scrapQuantity,
        Long operatorId
) {
}