package com.example.company.scrap.adapter.in.web.dto;

import java.time.LocalDateTime;

public record ScrapResponse(
        Long id,
        Long cuttingRecordId,
        int quantity,
        String reason,
        LocalDateTime createdAt
) {
}
