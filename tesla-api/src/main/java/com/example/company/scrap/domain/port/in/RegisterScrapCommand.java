package com.example.company.scrap.domain.port.in;

public record RegisterScrapCommand(
        Long cuttingRecordId,
        int quantity,
        String reason
) {
}