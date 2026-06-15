package com.example.company.inventory.adapter.out.persistence;

import com.example.company.inventory.domain.exception.InventoryItemNotFoundException;
import com.example.company.inventory.domain.model.InventoryItem;
import com.example.company.inventory.domain.model.InventoryItemStatus;
import com.example.company.inventory.domain.port.out.InventoryItemCreationPort;
import com.example.company.inventory.domain.port.out.InventoryItemRepositoryPort;
import com.example.company.inventory.domain.port.out.InventoryItemUpdatePort;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
class InventoryPersistenceAdapter implements InventoryItemCreationPort, InventoryItemUpdatePort, InventoryItemRepositoryPort {

    private final InventoryItemSpringRepository repository;
    private final InventoryPersistenceMapper mapper;

    InventoryPersistenceAdapter(InventoryItemSpringRepository repository, InventoryPersistenceMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    @Override
    public InventoryItem create(Long receptionId, int availableQuantity) {
        InventoryItemJpaEntity entity = mapper.toNewEntity(receptionId, availableQuantity);
        InventoryItemJpaEntity saved = repository.save(entity);
        return mapper.toDomain(saved);
    }

    @Override
    public void updateToCut(Long inventoryItemId, int goodQuantity) {
        InventoryItemJpaEntity entity = repository.findById(inventoryItemId)
                .orElseThrow(() -> new InventoryItemNotFoundException(inventoryItemId));
        entity.setAvailableQuantity(goodQuantity);
        entity.setStatus(InventoryItemStatus.CUT.name());
        entity.setUpdatedAt(LocalDateTime.now());
        repository.save(entity);
    }

    @Override
    public InventoryItem save(InventoryItem item) {
        InventoryItemJpaEntity entity;
        if (item.id() == null) {
            entity = mapper.toNewEntity(item.receptionId(), item.availableQuantity());
        } else {
            entity = repository.findById(item.id())
                    .orElseThrow(() -> new IllegalStateException("InventoryItem entity disappeared during save: " + item.id()));
            entity.setAvailableQuantity(item.availableQuantity());
            entity.setStatus(item.status().name());
            entity.setUpdatedAt(LocalDateTime.now());
        }
        InventoryItemJpaEntity saved = repository.save(entity);
        return mapper.toDomain(saved);
    }

    @Override
    public Optional<InventoryItem> findById(Long id) {
        return repository.findById(id).map(mapper::toDomain);
    }
}
