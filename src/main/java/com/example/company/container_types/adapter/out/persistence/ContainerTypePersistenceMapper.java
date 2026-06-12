package com.example.company.container_types.adapter.out.persistence;

import com.example.company.container_types.domain.model.ContainerType;
import org.springframework.stereotype.Component;

@Component
public class ContainerTypePersistenceMapper {

    ContainerType toDomain(ContainerTypeJpaEntity entity) {
        return ContainerType.restore(
                entity.getId(),
                entity.getName(),
                entity.isActive()
        );
    }

    ContainerTypeJpaEntity toNewEntity(ContainerType containerType) {
        return new ContainerTypeJpaEntity(
                containerType.name(),
                containerType.active()
        );
    }
}
