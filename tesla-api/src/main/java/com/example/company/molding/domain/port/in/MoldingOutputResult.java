package com.example.company.molding.domain.port.in;

import java.time.LocalDateTime;

public record MoldingOutputResult(
        Long id,
        Long cuttingRecordId,
        int quantitySent,
        Long operatorId,
        LocalDateTime sentAt
) {
}