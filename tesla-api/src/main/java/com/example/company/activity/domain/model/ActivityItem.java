package com.example.company.activity.domain.model;

import java.time.LocalDateTime;

public record ActivityItem(
        Long id,
        String time,
        String containerCode,
        String profileCode,
        String action,
        String quantities,
        String status,
        LocalDateTime timestamp
) {
}
