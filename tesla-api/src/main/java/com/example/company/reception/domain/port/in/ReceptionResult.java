package com.example.company.reception.domain.port.in;

import java.time.LocalDateTime;

public record ReceptionResult(
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