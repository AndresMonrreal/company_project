---
name: hexagonal-domain-developer
description: Use when creating or modifying pure domain code in tesla-api — aggregates, value objects, domain events, domain exceptions, and input/output ports. Real package base: com.example.company.<module>.domain. Never import Spring, JPA, Jackson, or adapters here.
---

# Hexagonal Domain Developer

Write pure Java. Domain code is the business core and must not import Spring, JPA, Jackson, Servlet, GraphQL, DTOs, repositories, or adapters.

## Package Contract

```text
com.empresa.app.<module>.domain.model
com.empresa.app.<module>.domain.event
com.empresa.app.<module>.domain.exception
com.empresa.app.<module>.domain.port.in
com.empresa.app.<module>.domain.port.out
com.empresa.app.<module>.domain.service
```

## Wrong: Domain As JPA

```java
package com.empresa.app.cutting.domain.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;

@Entity
@Table(name = "cutting_records")
public class CuttingRecord {
    private int initialQuantity;
    private int goodQuantity;
    private int scrapQuantity;
}
```

Bug: persistence annotations leak into business code; domain tests now depend on Hibernate behavior and database column changes.

## Correct: Aggregate With Invariant And Event

```java
package com.empresa.app.cutting.domain.model;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import com.empresa.app.cutting.domain.event.CuttingRecorded;

public final class CuttingRecord {
    private final Long id;
    private final Long inventoryItemId;
    private final Long machineId;
    private final Long operatorId;
    private final Long shiftId;
    private final CuttingQuantities quantities;
    private final Instant cutAt;
    private final List<Object> domainEvents = new ArrayList<>();

    private CuttingRecord(Long id, Long inventoryItemId, Long machineId, Long operatorId,
                          Long shiftId, CuttingQuantities quantities, Instant cutAt) {
        this.id = id;
        this.inventoryItemId = requireId(inventoryItemId, "inventory item is required");
        this.machineId = requireId(machineId, "machine is required");
        this.operatorId = requireId(operatorId, "operator is required");
        this.shiftId = requireId(shiftId, "shift is required");
        this.quantities = quantities;
        this.cutAt = cutAt;
    }

    public static CuttingRecord record(Long inventoryItemId, Long machineId, Long operatorId,
                                       Long shiftId, CuttingQuantities quantities, Instant cutAt) {
        CuttingRecord cutting = new CuttingRecord(null, inventoryItemId, machineId, operatorId, shiftId, quantities, cutAt);
        cutting.domainEvents.add(new CuttingRecorded(inventoryItemId, quantities.goodQuantity(), quantities.scrapQuantity()));
        return cutting;
    }

    public static CuttingRecord restore(Long id, Long inventoryItemId, Long machineId, Long operatorId,
                                        Long shiftId, CuttingQuantities quantities, Instant cutAt) {
        return new CuttingRecord(id, inventoryItemId, machineId, operatorId, shiftId, quantities, cutAt);
    }

    public List<Object> pullDomainEvents() {
        List<Object> events = List.copyOf(domainEvents);
        domainEvents.clear();
        return events;
    }

    private static Long requireId(Long value, String message) {
        if (value == null) {
            throw new IllegalArgumentException(message);
        }
        return value;
    }
}
```

```java
package com.empresa.app.cutting.domain.model;

public record CuttingQuantities(int initialQuantity, int goodQuantity, int scrapQuantity) {
    public CuttingQuantities {
        if (initialQuantity <= 0) {
            throw new IllegalArgumentException("initial_quantity must be greater than zero");
        }
        if (goodQuantity < 0 || scrapQuantity < 0) {
            throw new IllegalArgumentException("cut quantities must not be negative");
        }
        if (initialQuantity != goodQuantity + scrapQuantity) {
            throw new IllegalArgumentException("initial_quantity must equal good_quantity + scrap_quantity");
        }
    }
}
```

```java
package com.empresa.app.cutting.domain.event;

public record CuttingRecorded(Long inventoryItemId, int goodQuantity, int scrapQuantity) {
}
```

## Ports Belong To Domain

Wrong:

```java
package com.empresa.app.cutting.application.usecase;

public class CreateCuttingService {
    private final SpringDataCuttingRepository repository;
}
```

Bug: the use case depends on infrastructure and cannot swap persistence.

Correct:

```java
package com.empresa.app.cutting.domain.port.out;

import com.empresa.app.cutting.domain.model.CuttingRecord;

public interface SaveCuttingPort {
    CuttingRecord save(CuttingRecord cutting);
}
```

```java
package com.empresa.app.cutting.domain.port.in;

public interface CreateCuttingUseCase {
    CuttingResult create(CreateCuttingCommand command);
}
```

## Domain Service Versus Application Use Case

Use a domain service only for pure business logic involving multiple aggregates and no infrastructure.

```java
package com.empresa.app.cutting.domain.service;

public final class CuttingPolicy {
    public boolean canSendToMolding(CuttingQuantities quantities) {
        return quantities.goodQuantity() > 0;
    }
}
```

Application use cases orchestrate transactions, ports, authorization, and persistence. Never put `@Transactional` in domain.
