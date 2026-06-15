package com.example.company.molding.adapter.out.persistence;

import com.example.company.molding.domain.model.MoldingOutput;
import org.springframework.stereotype.Component;

@Component
class MoldingOutputPersistenceMapper {

    MoldingOutputJpaEntity toNewEntity(MoldingOutput output) {
        return new MoldingOutputJpaEntity(
                output.id(),
                output.cuttingRecordId(),
                output.quantitySent(),
                output.operatorId(),
                output.sentAt()
        );
    }

    MoldingOutput toDomain(MoldingOutputJpaEntity entity) {
        return MoldingOutput.restore(
                entity.getId(),
                entity.getCuttingRecordId(),
                entity.getOperatorId(),
                entity.getQuantitySent(),
                entity.getSentAt()
        );
    }
}
