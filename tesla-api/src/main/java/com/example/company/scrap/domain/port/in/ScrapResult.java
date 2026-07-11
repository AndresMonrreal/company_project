package com.example.company.scrap.domain.port.in;

import java.time.LocalDateTime;

public record ScrapResult(
        Long id,
        Long shiftId,
        String shiftName,
        Long profileId,
        String profileCode,
        Long operatorId,
        int quantity,
        String reason,
        LocalDateTime createdAt
) {
}