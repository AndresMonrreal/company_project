package com.example.company.inventory.adapter.out.persistence;

import com.example.company.inventory.domain.model.InventoryItem;
import com.example.company.inventory.domain.model.InventoryItemStatus;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
class InventoryPersistenceMapper {

    InventoryItem toDomain(InventoryItemJpaEntity entity) {
        return InventoryItem.restore(
                entity.getId(),
                entity.getReceptionId(),
                entity.getAvailableQuantity(),
                InventoryItemStatus.valueOf(entity.getStatus())
        );
    }

    InventoryItemJpaEntity toNewEntity(Long receptionId, int availableQuantity) {
        return new InventoryItemJpaEntity(
                receptionId,
                availableQuantity,
                InventoryItemStatus.AVAILABLE.name(),
                0L,
                LocalDateTime.now()
        );
    }
}
