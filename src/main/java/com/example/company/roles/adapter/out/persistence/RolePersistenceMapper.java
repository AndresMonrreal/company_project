package com.example.company.roles.adapter.out.persistence;

import com.example.company.roles.domain.model.Role;
import org.springframework.stereotype.Component;

@Component
public class RolePersistenceMapper {

    Role toDomain(RoleJpaEntity entity) {
        return Role.restore(
                entity.getId(),
                entity.getName(),
                entity.getDescription(),
                entity.isActive()
        );
    }

    RoleJpaEntity toNewEntity(Role role) {
        return new RoleJpaEntity(
                role.name(),
                role.description(),
                role.active()
        );
    }
}
