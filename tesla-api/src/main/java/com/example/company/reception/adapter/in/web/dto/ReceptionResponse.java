package com.example.company.reception.adapter.in.web.dto;

import java.time.LocalDateTime;

public record ReceptionResponse(
        Long id,
        Long containerId,
        Long profileId,
        Long operatorId,
        String lot,
        int receivedQuantity,
        String status,
        LocalDateTime receivedAt
) {
}