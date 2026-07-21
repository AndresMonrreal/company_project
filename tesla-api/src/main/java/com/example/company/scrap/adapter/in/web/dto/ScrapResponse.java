package com.example.company.scrap.adapter.in.web.dto;

import java.time.LocalDateTime;

public record ScrapResponse(
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