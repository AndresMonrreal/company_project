package com.example.company.profiles.adapter.out.persistence;

import com.example.company.profiles.domain.model.Profile;
import org.springframework.stereotype.Component;

@Component
public class ProfilePersistenceMapper {

    Profile toDomain(ProfileJpaEntity entity) {
        return Profile.restore(
                entity.getId(),
                entity.getCode(),
                entity.getName(),
                entity.getDescription(),
                entity.isActive(),
                entity.getType(),
                entity.getPosition()
        );
    }

    ProfileJpaEntity toNewEntity(Profile profile) {
        return new ProfileJpaEntity(
                profile.code(),
                profile.name(),
                profile.description(),
                profile.active(),
                profile.type(),
                profile.position()
        );
    }
}
