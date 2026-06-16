package com.example.company.cutting.adapter.in.web.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record CuttingRequest(
        @NotNull Long inventoryItemId,
        @NotNull Long machineId,
        @NotNull Long shiftId,
        @Min(1) int initialQuantity,
        @Min(0) int goodQuantity,
        @Min(0) int scrapQuantity
) {
}