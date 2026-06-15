package com.example.company.cutting.adapter.in.web.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;

public record RegisterCutRequest(
        @NotNull Long inventoryItemId,
        @NotNull Long machineId,
        @NotNull Long shiftId,
        @Positive int initialQuantity,
        @PositiveOrZero int goodQuantity,
        @PositiveOrZero int scrapQuantity
) {
}
