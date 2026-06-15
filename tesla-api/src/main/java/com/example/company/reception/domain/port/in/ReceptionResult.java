package com.example.company.reception.domain.port.in;

import java.time.LocalDateTime;

public record ReceptionResult(
        Long id,
        String containerCode,
        String profileCode,
        String lot,
        int receivedQuantity,
        String status,
        LocalDateTime receivedAt
) {
}
