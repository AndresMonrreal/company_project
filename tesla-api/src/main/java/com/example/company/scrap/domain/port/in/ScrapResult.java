package com.example.company.scrap.domain.port.in;

import java.time.LocalDateTime;

public record ScrapResult(
        Long id,
        Long cuttingRecordId,
        int quantity,
        String reason,
        LocalDateTime createdAt
) {
}