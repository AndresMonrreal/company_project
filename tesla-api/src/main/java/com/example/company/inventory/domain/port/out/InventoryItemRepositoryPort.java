package com.example.company.inventory.domain.port.out;

import com.example.company.inventory.domain.model.InventoryItem;
import java.util.Optional;

public interface InventoryItemRepositoryPort {

    InventoryItem save(InventoryItem item);

    Optional<InventoryItem> findById(Long id);
}
