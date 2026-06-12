---
name: refactor-to-hexagonal
description: Refactor existing Spring Boot controller-service-repository code into Hexagonal Architecture. Use when moving modules such as profiles, cutting, inventory, reception, scrap, or molding into domain model, domain ports, application use cases, web/GraphQL adapters, persistence adapters, and tests without changing behavior.
---

# Refactor To Hexagonal

Migrate one module at a time.

## Ordered Workflow

1. Freeze endpoint paths and response shape.
2. Extract pure domain model from JPA entity.
3. Move input and output ports to `domain/port/in` and `domain/port/out`.
4. Split large service into one use case class per operation.
5. Move JPA entity to `adapter/out/persistence`.
6. Add persistence mapper and adapter implementing output port.
7. Move controller to `adapter/in/web`.
8. Move request/response DTOs under `adapter/in/web/dto`.
9. Add or update tests.

## Wrong Layered Code

```java
@Service
public class ProfileService {
    private final ProfileRepository repository;

    public ProfileResponse create(ProfileCreateRequest request) {
        if (repository.existsByCode(request.code())) {
            throw new DuplicateResourceException("Profile code already exists");
        }
        return ProfileMapper.toResponse(repository.save(new Profile(request.code(), request.name(), null)));
    }
}
```

Bug: service knows web DTOs and Spring Data.

## Correct Hexagonal Code

```java
@Service
public class CreateProfileService implements CreateProfileUseCase {
    private final ProfileRepositoryPort profiles;

    @Transactional
    public ProfileResult create(CreateProfileCommand command) {
        if (profiles.existsByCode(command.code())) {
            throw new DuplicateProfileCodeException(command.code());
        }
        return ProfileResultMapper.toResult(profiles.save(Profile.create(command.code(), command.name(), command.description())));
    }
}
```

## JPA Split

Wrong:

```java
package com.empresa.app.profiles.domain.model;

@Entity
public class Profile {
}
```

Correct:

```java
package com.empresa.app.profiles.adapter.out.persistence;

@Entity
@Table(name = "profiles")
class ProfileJpaEntity {
}
```

## Verification

Run:

```powershell
.\gradlew.bat compileJava testClasses
.\gradlew.bat test --tests "*<ChangedDomainTest>"
```

Report old files removed, new files created, behavior preserved, and tests run.
