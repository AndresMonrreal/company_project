package com.example.company.scrap.domain.port.in;

public record RegisterScrapCommand(
        Long shiftId,
        Long profileId,
        Long operatorId,
        int quantity,
        String reason
) {
}