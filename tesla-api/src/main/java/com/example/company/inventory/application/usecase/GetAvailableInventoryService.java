package com.example.company.inventory.application.usecase;

import com.example.company.inventory.domain.exception.InventoryNotAvailableException;
import com.example.company.inventory.domain.model.AvailableInventoryResult;
import com.example.company.inventory.domain.port.in.GetAvailableInventoryUseCase;
import com.example.company.inventory.domain.port.out.InventoryItemRepositoryPort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class GetAvailableInventoryService implements GetAvailableInventoryUseCase {

    private final InventoryItemRepositoryPort inventoryRepository;

    public GetAvailableInventoryService(InventoryItemRepositoryPort inventoryRepository) {
        this.inventoryRepository = inventoryRepository;
    }

    @Override
    public AvailableInventoryResult findAvailableByContainerCode(String containerCode) {
        return inventoryRepository.findAvailableByContainerCode(containerCode)
                .orElseThrow(() -> new InventoryNotAvailableException(containerCode));
    }
}