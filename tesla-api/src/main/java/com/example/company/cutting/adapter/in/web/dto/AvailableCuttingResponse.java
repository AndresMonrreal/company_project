package com.example.company.cutting.adapter.in.web.dto;

import java.time.LocalDateTime;

public record AvailableCuttingResponse(
        Long cuttingRecordId,
        String containerCode,
        String profileCode,
        int initialQuantity,
        int goodQuantity,
        int scrapQuantity,
        LocalDateTime cutAt
) {
}