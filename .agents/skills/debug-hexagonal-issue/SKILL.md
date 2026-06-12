---
name: debug-hexagonal-issue
description: Debug common Hexagonal Architecture and Spring Boot failures in this manufacturing backend, including wrong dependency direction, LazyInitializationException, @Transactional not applying, ArchUnit failures, GraphQL N+1, missing port beans, circular dependencies, Flyway mismatches, and invalid cutting quantities.
---

# Debug Hexagonal Issue

Debug by layer: adapter -> input port -> use case -> domain -> output port -> persistence adapter.

## Wrong Dependency Direction

Symptom: ArchUnit failure or domain imports `adapter`.

Wrong:

```java
import com.empresa.app.inventory.adapter.out.persistence.InventoryJpaEntity;
```

Fix:

```java
import com.empresa.app.inventory.domain.model.InventoryItem;
import com.empresa.app.inventory.domain.port.out.LoadInventoryItemPort;
```

## LazyInitializationException

Symptom: serialization or mapper fails outside transaction.

Wrong:

```java
record.getInventoryItem().getReception().getLot();
```

Fix with fetch plan:

```java
@EntityGraph(attributePaths = {"inventoryItem", "inventoryItem.reception"})
List<CuttingRecordJpaEntity> findByShiftId(Long shiftId);
```

## @Transactional Not Working

Symptom: updates do not flush or lazy state loads outside transaction.

Wrong:

```java
public void outer() {
    this.innerTransactionalMethod();
}

@Transactional
public void innerTransactionalMethod() {
}
```

Bug: self-invocation bypasses Spring proxy.

Fix:

```java
@Service
class UpdateProfileService implements UpdateProfileUseCase {
    @Transactional
    public ProfileResult update(Long id, UpdateProfileCommand command) {
        // transaction starts at external proxy call
    }
}
```

## Port Not Wired

Symptom: `NoSuchBeanDefinitionException` for an interface.

Wrong:

```java
class ProfilePersistenceAdapter implements ProfileRepositoryPort {
}
```

Fix:

```java
@Repository
class ProfilePersistenceAdapter implements ProfileRepositoryPort {
}
```

## Circular Dependency

Wrong:

```java
class ProfileRestController {
    ProfilePersistenceAdapter adapter;
}

class ProfilePersistenceAdapter {
    ProfileRestController controller;
}
```

Fix: controller depends on input port; persistence adapter implements output port.

## GraphQL N+1

Wrong:

```java
@SchemaMapping
InventoryPayload inventory(CuttingPayload cutting) {
    return inventoryRepository.findById(cutting.inventoryItemId()).orElseThrow();
}
```

Fix: batch with `@BatchMapping` or DataLoader.

## Cutting Rule Failure

Symptom: PostgreSQL rejects `cutting_quantity_rule`.

Fix: instantiate `CuttingQuantities` in domain before save.

```java
new CuttingQuantities(command.initialQuantity(), command.goodQuantity(), command.scrapQuantity());
```

## Output

Return symptom, layer, root cause, exact fix, files touched, and verification command.
