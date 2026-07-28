package com.example.company.inventory.domain.port.out;

import java.util.Optional;

import com.example.company.inventory.domain.model.AvailableInventoryResult;

public interface InventoryItemRepositoryPort {

    Optional<AvailableInventoryResult> findAvailableByLot(String lot);
}