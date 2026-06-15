package com.example.company.reception.adapter.in.web.dto;

import java.time.LocalDateTime;

public record ReceptionResponse(
        Long id,
        String containerCode,
        String profileCode,
        String lot,
        int receivedQuantity,
        String status,
        LocalDateTime receivedAt
) {
}
