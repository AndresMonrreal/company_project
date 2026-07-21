package com.example.company.scrap.adapter.in.web.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record ScrapRequest(
        @NotNull Long shiftId,
        @NotNull Long profileId,
        @Min(1) int quantity,
        @Size(max = 255) String reason
) {
}