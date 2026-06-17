package com.example.company.cutting.adapter.in.web.dto;

import java.time.LocalDateTime;

public record CuttingResponse(
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