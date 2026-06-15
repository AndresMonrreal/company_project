package com.example.company.scrap.domain.port.in;

public record RegisterScrapCommand(
        Long cuttingRecordId,
        Long operatorId,
        int quantity,
        String reason
) {
}
