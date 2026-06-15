package com.example.company.molding.adapter.in.web.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

public record RegisterMoldingOutputRequest(
        @NotNull Long cuttingRecordId,
        @PositiveOrZero int quantitySent
) {
}
