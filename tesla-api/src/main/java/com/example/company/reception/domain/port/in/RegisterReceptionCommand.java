package com.example.company.reception.domain.port.in;

public record RegisterReceptionCommand(
        Long containerId,
        Long profileId,
        String lot,
        int receivedQuantity,
        Long operatorId
) {
}