package com.example.company.reception.adapter.out.persistence;

import com.example.company.reception.domain.model.Reception;
import com.example.company.reception.domain.model.ReceptionStatus;
import org.springframework.stereotype.Component;

@Component
class ReceptionPersistenceMapper {

    Reception toDomain(ReceptionJpaEntity entity) {
        return Reception.restore(
                entity.getId(),
                entity.getContainerId(),
                entity.getProfileId(),
                entity.getOperatorId(),
                entity.getLot(),
                entity.getReceivedQuantity(),
                ReceptionStatus.valueOf(entity.getStatus()),
                entity.getReceivedAt()
        );
    }

    ReceptionJpaEntity toNewEntity(Reception domain) {
        return new ReceptionJpaEntity(
                domain.containerId(),
                domain.profileId(),
                domain.operatorId(),
                domain.lot(),
                domain.receivedQuantity(),
                domain.status().name(),
                domain.receivedAt()
        );
    }
}
