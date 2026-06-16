package com.example.company.molding.adapter.in.web.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record MoldingOutputRequest(
        @NotNull Long cuttingRecordId,
        @Min(1) int quantitySent
) {
}