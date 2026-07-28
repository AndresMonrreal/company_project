package com.example.company.activity.domain.model;

import java.time.LocalDateTime;

public record ActivityRawEntry(
        Long id,
        String containerCode,
        String profileCode,
        ActivityAction action,
        LocalDateTime recordedAt,
        int primaryQuantity,
        Integer secondaryQuantity,
        Integer tertiaryQuantity,
        String status,
        String lot
) {
}