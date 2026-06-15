package com.example.company.scrap.adapter.in.web.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

public record RegisterScrapRequest(
        @NotNull Long cuttingRecordId,
        @PositiveOrZero int quantity,
        String reason
) {
}
