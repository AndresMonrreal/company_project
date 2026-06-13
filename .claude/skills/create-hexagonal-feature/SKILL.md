---
name: create-hexagonal-feature
description: Create or extend a complete manufacturing feature in this Spring Boot hexagonal backend. Use for modules such as reception, inventory, cutting, scrap, molding, reports, profiles, machines, shifts, containers, auth, users, or roles with domain aggregates, domain ports, application use cases, JPA adapters, REST/GraphQL adapters, Flyway-aware persistence, and tests.
---

# Create Hexagonal Feature

Build one module at a time. Do not modify existing Flyway migrations.

## Step 1: Domain Model

Wrong:

```java
@Entity
public class CuttingRecord {
    private int initialQuantity;
}
```

Bug: JPA leaks into domain.

Correct:

```java
package com.empresa.app.cutting.domain.model;

public final class CuttingRecord {
    private final CuttingQuantities quantities;

    public static CuttingRecord record(CuttingQuantities quantities) {
        return new CuttingRecord(quantities);
    }
}
```

## Step 2: Output Port In Domain

```java
package com.empresa.app.cutting.domain.port.out;

public interface SaveCuttingPort {
    CuttingRecord save(CuttingRecord cuttingRecord);
}
```

## Step 3: Input Port In Domain

```java
package com.empresa.app.cutting.domain.port.in;

public interface CreateCuttingUseCase {
    CuttingResult create(CreateCuttingCommand command);
}
```

## Step 4: Use Case In Application

```java
package com.empresa.app.cutting.application.usecase;

@Service
public class CreateCuttingService implements CreateCuttingUseCase {
    private final SaveCuttingPort saveCutting;

    @Transactional
    public CuttingResult create(CreateCuttingCommand command) {
        CuttingQuantities quantities = new CuttingQuantities(
                command.initialQuantity(),
                command.goodQuantity(),
                command.scrapQuantity()
        );
        return CuttingResultMapper.toResult(saveCutting.save(CuttingRecord.record(quantities)));
    }
}
```

## Step 5: JPA Entity In Persistence Adapter

```java
package com.empresa.app.cutting.adapter.out.persistence;

@Entity
@Table(name = "cutting_records")
class CuttingRecordJpaEntity {
    @Column(name = "initial_quantity", nullable = false)
    private int initialQuantity;
}
```

## Step 6: Spring Data Repository

```java
interface SpringDataCuttingRepository extends JpaRepository<CuttingRecordJpaEntity, Long> {
}
```

## Step 7: Persistence Adapter

```java
@Repository
class CuttingPersistenceAdapter implements SaveCuttingPort {
    private final SpringDataCuttingRepository repository;
    private final CuttingPersistenceMapper mapper;

    public CuttingRecord save(CuttingRecord cutting) {
        return mapper.toDomain(repository.save(mapper.toEntity(cutting)));
    }
}
```

## Step 8: Flyway Migration

Only create a new migration when the schema is missing.

```sql
-- V2__add_cutting_notes.sql
ALTER TABLE cutting_records ADD COLUMN notes VARCHAR(255);
```

Never edit `V1__create_initial_schema.sql`.

## Step 9: REST DTOs

```java
public record CreateCuttingRequest(
        @NotNull Long inventoryItemId,
        @Positive int initialQuantity,
        @PositiveOrZero int goodQuantity,
        @PositiveOrZero int scrapQuantity
) {
}
```

## Step 10: REST Controller

```java
@RestController
@RequestMapping("/api/cutting")
class CuttingRestController {
    private final CreateCuttingUseCase createCutting;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    CuttingResponse create(@Valid @RequestBody CreateCuttingRequest request) {
        return mapper.toResponse(createCutting.create(mapper.toCommand(request)));
    }
}
```

## Step 11: GraphQL If Requested

```graphql
type Mutation {
  createCutting(input: CreateCuttingInput!): CuttingRecord!
}
```

Resolver calls `CreateCuttingUseCase`, never repository.

## Step 12: Tests

- Domain: pure JUnit.
- Use case: mocked output ports.
- Persistence: `@DataJpaTest`.
- Web: `@WebMvcTest`.
- Boundaries: ArchUnit.

## Output

Report files created, migration status, tests added, and verification commands.
