package com.example.company.cutting.adapter.out.persistence;

import com.example.company.cutting.domain.model.CuttingRecord;
import org.springframework.stereotype.Component;

@Component
class CuttingPersistenceMapper {

    CuttingRecordJpaEntity toNewEntity(CuttingRecord record) {
        return new CuttingRecordJpaEntity(
                record.inventoryItemId(),
                record.machineId(),
                record.operatorId(),
                record.shiftId(),
                record.quantities().initialQuantity(),
                record.quantities().goodQuantity(),
                record.quantities().scrapQuantity(),
                record.cutAt()
        );
    }

    CuttingRecord toDomain(CuttingRecordJpaEntity entity) {
        return CuttingRecord.restore(
                entity.getId(),
                entity.getInventoryItemId(),
                entity.getMachineId(),
                entity.getOperatorId(),
                entity.getShiftId(),
                entity.getInitialQuantity(),
                entity.getGoodQuantity(),
                entity.getScrapQuantity(),
                entity.getCutAt()
        );
    }
}
