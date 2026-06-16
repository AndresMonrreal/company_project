package com.example.company.inventory.adapter.out.persistence;

import java.util.Optional;

import com.example.company.inventory.domain.model.AvailableInventoryResult;
import com.example.company.inventory.domain.port.out.InventoryItemCreationPort;
import com.example.company.inventory.domain.port.out.InventoryItemRepositoryPort;
import com.example.company.inventory.domain.port.out.InventoryItemUpdatePort;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public class InventoryPersistenceAdapter
        implements InventoryItemCreationPort, InventoryItemUpdatePort, InventoryItemRepositoryPort {

    private final InventoryItemSpringRepository inventoryRepository;

    public InventoryPersistenceAdapter(InventoryItemSpringRepository inventoryRepository) {
        this.inventoryRepository = inventoryRepository;
    }

    @Override
    @Transactional
    public void createInventoryItem(Long receptionId, int availableQuantity) {
        inventoryRepository.save(new InventoryItemJpaEntity(receptionId, availableQuantity, "AVAILABLE"));
    }

    @Override
    @Transactional
    public void markAsCut(Long inventoryItemId) {
        inventoryRepository.markAsCut(inventoryItemId);
    }

    @Override
    public Optional<AvailableInventoryResult> findAvailableByContainerCode(String containerCode) {
        return inventoryRepository.findAvailableByContainerCode(containerCode)
                .map(this::mapToResult);
    }

    private AvailableInventoryResult mapToResult(Object[] row) {
        Object[] cols = (row[0] instanceof Object[]) ? (Object[]) row[0] : row;
        Long inventoryItemId = ((Number) cols[0]).longValue();
        String containerCode = (String) cols[1];
        String profileCode = (String) cols[2];
        String lot = (String) cols[3];
        int availableQuantity = ((Number) cols[4]).intValue();
        return new AvailableInventoryResult(inventoryItemId, containerCode, profileCode, lot, availableQuantity);
    }
}