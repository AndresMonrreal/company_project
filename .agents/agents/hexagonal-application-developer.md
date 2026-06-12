---
name: hexagonal-application-developer
description: Use when implementing application/usecase classes, transaction boundaries, use case orchestration, command/result mapping, and dependency inversion wiring between domain ports and adapters.
---

# Hexagonal Application Developer

Use case classes live in `application/usecase`. Prefer one class per use case. `@Transactional` belongs here, never in domain.

## Package Contract

```text
com.empresa.app.<module>.application.usecase
com.empresa.app.<module>.application.mapper
```

Ports are in `domain/port/in` and `domain/port/out`.

## Wrong: One Service With Web DTOs And Repository

```java
package com.empresa.app.profiles.application.usecase;

@Service
public class ProfileService {
    private final ProfileJpaRepository repository;

    public ProfileResponse create(ProfileCreateRequest request) {
        return ProfileMapper.toResponse(repository.save(new ProfileJpaEntity(request.code(), request.name())));
    }
}
```

Bug: the application layer knows REST DTOs and JPA. GraphQL, tests, and future adapters cannot reuse the use case cleanly.

## Correct: One Use Case Class With Port Injection

```java
package com.empresa.app.profiles.application.usecase;

import com.empresa.app.profiles.domain.exception.DuplicateProfileCodeException;
import com.empresa.app.profiles.domain.model.Profile;
import com.empresa.app.profiles.domain.port.in.CreateProfileCommand;
import com.empresa.app.profiles.domain.port.in.CreateProfileUseCase;
import com.empresa.app.profiles.domain.port.in.ProfileResult;
import com.empresa.app.profiles.domain.port.out.ProfileRepositoryPort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CreateProfileService implements CreateProfileUseCase {
    private final ProfileRepositoryPort profiles;

    public CreateProfileService(ProfileRepositoryPort profiles) {
        this.profiles = profiles;
    }

    @Override
    @Transactional
    public ProfileResult create(CreateProfileCommand command) {
        if (profiles.existsByCode(command.code())) {
            throw new DuplicateProfileCodeException(command.code());
        }
        Profile profile = Profile.create(command.code(), command.name(), command.description());
        return ProfileResultMapper.toResult(profiles.save(profile));
    }
}
```

## Command And Result Are Not Domain Objects

Correct command:

```java
package com.empresa.app.cutting.domain.port.in;

public record CreateCuttingCommand(Long inventoryItemId, Long machineId, Long operatorId,
                                   Long shiftId, int initialQuantity, int goodQuantity, int scrapQuantity) {
}
```

Correct result:

```java
package com.empresa.app.cutting.domain.port.in;

public record CuttingResult(Long id, int initialQuantity, int goodQuantity, int scrapQuantity) {
}
```

Bug avoided: external adapters do not mutate aggregate internals directly.

## Orchestration Example

```java
@Service
public class CreateCuttingService implements CreateCuttingUseCase {
    private final LoadInventoryItemPort loadInventoryItem;
    private final SaveCuttingPort saveCutting;

    @Override
    @Transactional
    public CuttingResult create(CreateCuttingCommand command) {
        InventoryItem item = loadInventoryItem.load(command.inventoryItemId())
                .orElseThrow(() -> new InventoryItemNotFoundException(command.inventoryItemId()));

        CuttingQuantities quantities = new CuttingQuantities(
                command.initialQuantity(),
                command.goodQuantity(),
                command.scrapQuantity()
        );

        CuttingRecord cutting = CuttingRecord.record(
                item.id(),
                command.machineId(),
                command.operatorId(),
                command.shiftId(),
                quantities,
                Instant.now()
        );

        return CuttingResultMapper.toResult(saveCutting.save(cutting));
    }
}
```

Checklist:

- Inject output ports, not adapters.
- Use input port interfaces as the public API of the use case.
- Keep authorization checks here when they must apply to every adapter.
- Never return JPA entities.
