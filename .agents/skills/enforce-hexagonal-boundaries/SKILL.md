---
name: enforce-hexagonal-boundaries
description: Enforce Hexagonal Architecture boundaries in this Spring Boot backend. Use when reviewing imports, package structure, dependency direction, domain purity, adapter isolation, repository bypasses, and ArchUnit architecture rules.
---

# Enforce Hexagonal Boundaries

Use manual `rg` checks now and ArchUnit when dependency is available.

## Manual Checks

```powershell
rg "import (jakarta.persistence|org.springframework|com.example.company..*adapter)" src/main/java/com/example/company -g "**/domain/**"
rg "import com.example.company..*adapter" src/main/java/com/example/company -g "**/application/**"
rg "Repository" src/main/java/com/example/company -g "**/adapter/in/**"
```

## Full ArchUnit Example

```java
package com.empresa.app.architecture;

import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

@AnalyzeClasses(packages = "com.empresa.app", importOptions = ImportOption.DoNotIncludeTests.class)
class HexagonalArchitectureTest {
    @ArchTest
    static final ArchRule domain_must_not_depend_on_frameworks_or_adapters =
            noClasses().that().resideInAPackage("..domain..")
                    .should().dependOnClassesThat().resideInAnyPackage(
                            "org.springframework..",
                            "jakarta.persistence..",
                            "..adapter.."
                    );

    @ArchTest
    static final ArchRule application_must_not_depend_on_adapters =
            noClasses().that().resideInAPackage("..application..")
                    .should().dependOnClassesThat().resideInAnyPackage("..adapter..");

    @ArchTest
    static final ArchRule inbound_adapters_must_not_depend_on_outbound_adapters =
            noClasses().that().resideInAPackage("..adapter.in..")
                    .should().dependOnClassesThat().resideInAPackage("..adapter.out..");
}
```

## Wrong

```java
package com.empresa.app.inventory.application.usecase;

import com.empresa.app.inventory.adapter.out.persistence.InventoryJpaEntity;
```

Bug: application code changes when persistence mapping changes.

## Correct

```java
package com.empresa.app.inventory.application.usecase;

import com.empresa.app.inventory.domain.model.InventoryItem;
import com.empresa.app.inventory.domain.port.out.LoadInventoryItemPort;
```

## When A Rule Fails

1. Identify source package and target package.
2. Replace concrete adapter dependency with a domain port.
3. Move DTO/JPA/HTTP classes back to adapter packages.
4. Re-run targeted architecture test.
