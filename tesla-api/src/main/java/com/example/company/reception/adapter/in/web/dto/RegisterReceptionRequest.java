package com.example.company.reception.adapter.in.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record RegisterReceptionRequest(
        @NotNull Long containerId,
        @NotNull Long profileId,
        @NotBlank String lot,
        @Positive int receivedQuantity
) {
}
