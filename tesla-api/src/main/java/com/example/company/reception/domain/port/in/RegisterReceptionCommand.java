package com.example.company.reception.domain.port.in;

public record RegisterReceptionCommand(
        Long containerId,
        Long profileId,
        Long operatorId,
        String lot,
        int receivedQuantity
) {
}
