package com.example.company.reception.adapter.in.web.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record ReceptionRequest(
        @NotNull Long containerId,
        @NotNull Long profileId,
        @NotBlank @Size(max = 80) String lot,
        @Min(1) int receivedQuantity
) {
}