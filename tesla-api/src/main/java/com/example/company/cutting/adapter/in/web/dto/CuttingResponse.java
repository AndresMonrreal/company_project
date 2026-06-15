package com.example.company.cutting.adapter.in.web.dto;

import java.time.LocalDateTime;

public record CuttingResponse(
        Long id,
        String containerCode,
        String profileCode,
        String machineCode,
        int initialQuantity,
        int goodQuantity,
        int scrapQuantity,
        LocalDateTime cutAt
) {
}
