package com.example.company.containers.adapter.out.persistence;

import com.example.company.containers.domain.model.Container;
import org.springframework.stereotype.Component;

@Component
public class ContainerPersistenceMapper {

    Container toDomain(ContainerJpaEntity entity) {
        return Container.restore(
                entity.getId(),
                entity.getContainerTypeId(),
                entity.getCode(),
                entity.isActive()
        );
    }

    ContainerJpaEntity toNewEntity(Container container) {
        return new ContainerJpaEntity(
                container.containerTypeId(),
                container.code(),
                container.active()
        );
    }
}
