package com.example.company.molding.adapter.in.web.dto;

import java.time.LocalDateTime;

public record MoldingOutputResponse(
        Long id,
        Long cuttingRecordId,
        int quantitySent,
        Long operatorId,
        LocalDateTime sentAt
) {
}