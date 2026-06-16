package com.example.company.inventory.domain.port.in;

import com.example.company.inventory.domain.model.AvailableInventoryResult;

public interface GetAvailableInventoryUseCase {

    AvailableInventoryResult findAvailableByContainerCode(String containerCode);
}