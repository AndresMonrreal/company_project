package com.example.company.cutting.domain.port.in;

import java.time.LocalDateTime;

public record CuttingResult(
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
