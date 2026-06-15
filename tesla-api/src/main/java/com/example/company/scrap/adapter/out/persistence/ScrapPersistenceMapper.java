package com.example.company.scrap.adapter.out.persistence;

import java.time.LocalDateTime;

import com.example.company.scrap.domain.model.ScrapRecord;
import org.springframework.stereotype.Component;

@Component
class ScrapPersistenceMapper {

    ScrapRecordJpaEntity toNewEntity(ScrapRecord record) {
        ScrapRecordJpaEntity entity = new ScrapRecordJpaEntity();
        entity.setCuttingRecordId(record.cuttingRecordId());
        entity.setQuantity(record.quantity());
        entity.setReason(record.reason());
        entity.setCreatedAt(record.createdAt() != null ? record.createdAt() : LocalDateTime.now());
        return entity;
    }

    ScrapRecord toDomain(ScrapRecordJpaEntity entity) {
        return ScrapRecord.restore(
                entity.getId(),
                entity.getCuttingRecordId(),
                entity.getQuantity(),
                entity.getReason(),
                entity.getCreatedAt()
        );
    }
}
