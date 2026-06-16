package com.example.company.molding.domain.port.in;

public record RegisterMoldingOutputCommand(
        Long cuttingRecordId,
        int quantitySent,
        Long operatorId
) {
}