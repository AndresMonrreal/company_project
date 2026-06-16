package com.example.company.cutting.domain.model;

import java.time.LocalDateTime;

public record AvailableCuttingResult(
        Long cuttingRecordId,
        String containerCode,
        String profileCode,
        int initialQuantity,
        int goodQuantity,
        int scrapQuantity,
        LocalDateTime cutAt
) {
}