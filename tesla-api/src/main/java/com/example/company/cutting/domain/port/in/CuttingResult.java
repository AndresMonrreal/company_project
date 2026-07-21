package com.example.company.cutting.domain.port.in;

import java.time.LocalDateTime;

public record CuttingResult(
        Long id,
        Long inventoryItemId,
        Long machineId,
        Long operatorId,
        Long shiftId,
        int initialQuantity,
        int goodQuantity,
        int scrapQuantity,
        LocalDateTime cutAt,
        String containerCode,
        String profileCode,
        String machineName
) {
}