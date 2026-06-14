---
name: hexagonal-persistence-adapter
description: Use when creating JPA entities, Spring Data repositories, persistence mappers, Flyway migrations, and fixing N+1 queries in tesla-api. Real package base: com.example.company.<module>.adapter.out.persistence. Never modify existing Flyway migrations — append only.
---

# Hexagonal Persistence Adapter

Persistence code lives in `adapter/out/persistence`. It implements domain output ports. Domain and application never depend on Spring Data repositories.

## Required Package

```text
com.empresa.app.<module>.adapter.out.persistence
```

## Wrong: JPA Entity In Domain

```java
package com.empresa.app.inventory.domain.model;

@Entity
@Table(name = "inventory_items")
public class InventoryItem {
    @ManyToOne(fetch = FetchType.LAZY)
    private ReceptionJpaEntity reception;
}
```

Bug: the domain now depends on Hibernate, lazy proxies, and database mapping details.

## Correct: Domain Port + JPA Adapter

```java
package com.empresa.app.inventory.domain.port.out;

import java.util.Optional;
import com.empresa.app.inventory.domain.model.InventoryItem;

public interface LoadInventoryItemPort {
    Optional<InventoryItem> load(Long id);
}
```

```java
package com.empresa.app.inventory.adapter.out.persistence;

@Entity
@Table(name = "inventory_items")
class InventoryItemJpaEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "available_quantity", nullable = false)
    private int availableQuantity;

    @Column(nullable = false, length = 40)
    private String status;
}
```

```java
package com.empresa.app.inventory.adapter.out.persistence;

interface SpringDataInventoryRepository extends JpaRepository<InventoryItemJpaEntity, Long> {
}
```

```java
package com.empresa.app.inventory.adapter.out.persistence;

@Repository
class InventoryPersistenceAdapter implements LoadInventoryItemPort {
    private final SpringDataInventoryRepository repository;
    private final InventoryPersistenceMapper mapper;

    InventoryPersistenceAdapter(SpringDataInventoryRepository repository, InventoryPersistenceMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    @Override
    public Optional<InventoryItem> load(Long id) {
        return repository.findById(id).map(mapper::toDomain);
    }
}
```

## Mapper Rule

Wrong:

```java
return new InventoryItemJpaEntity(domain.id(), domain.availableQuantity());
```

Bug: constructors often miss JPA-managed fields, versioning, or relationships.

Correct:

```java
class InventoryPersistenceMapper {
    InventoryItem toDomain(InventoryItemJpaEntity entity) {
        return InventoryItem.restore(entity.getId(), entity.getAvailableQuantity(), entity.getStatus());
    }
}
```

## Flyway Rule

Wrong:

```sql
-- Edit V1__create_initial_schema.sql after it has been shared
ALTER TABLE cutting_records ADD COLUMN notes VARCHAR(255);
```

Bug: Flyway checksum mismatch breaks existing databases.

Correct:

```sql
-- V2__add_cutting_notes.sql
ALTER TABLE cutting_records ADD COLUMN notes VARCHAR(255);
```

## N+1 Rule

Wrong:

```java
List<CuttingRecordJpaEntity> records = repository.findAll();
records.forEach(record -> record.getInventoryItem().getReception().getLot());
```

Bug: each record can trigger extra queries for inventory and reception.

Correct with `@EntityGraph`:

```java
interface SpringDataCuttingRepository extends JpaRepository<CuttingRecordJpaEntity, Long> {
    @EntityGraph(attributePaths = {"inventoryItem", "inventoryItem.reception"})
    List<CuttingRecordJpaEntity> findByShiftId(Long shiftId);
}
```

Correct with fetch join:

```java
@Query("""
    select c
    from CuttingRecordJpaEntity c
    join fetch c.inventoryItem i
    join fetch i.reception r
    where c.shiftId = :shiftId
    """)
List<CuttingRecordJpaEntity> findDetailedByShiftId(Long shiftId);
```

## Swap Adapter Rule

If persistence changes from PostgreSQL/JPA to another store, replace only `adapter/out/persistence`. Domain ports and use cases stay unchanged.
