---
name: hexagonal-test-engineer
description: Use when writing or reviewing domain tests, use case tests with mocked ports, adapter tests, full integration tests, and ArchUnit rules that enforce hexagonal boundaries.
---

# Hexagonal Test Engineer

Test from smallest layer outward.

## Domain Test: No Spring

Wrong:

```java
@SpringBootTest
class CuttingQuantitiesTest {
}
```

Bug: a pure invariant test now depends on application context and environment.

Correct:

```java
class CuttingQuantitiesTest {
    @Test
    void rejectsInvalidCuttingQuantities() {
        assertThatThrownBy(() -> new CuttingQuantities(100, 98, 3))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("initial_quantity must equal good_quantity + scrap_quantity");
    }
}
```

## Use Case Test: Mock Output Ports

```java
class CreateProfileServiceTest {
    private final ProfileRepositoryPort profiles = mock(ProfileRepositoryPort.class);
    private final CreateProfileService service = new CreateProfileService(profiles);

    @Test
    void rejectsDuplicateCode() {
        when(profiles.existsByCode("36")).thenReturn(true);

        assertThatThrownBy(() -> service.create(new CreateProfileCommand("36", "Front", null)))
                .isInstanceOf(DuplicateProfileCodeException.class);
    }
}
```

## Adapter Tests

Persistence:

```java
@DataJpaTest
class ProfilePersistenceAdapterTest {
    @Autowired SpringDataProfileRepository repository;

    @Test
    void storesProfileJpaEntity() {
        ProfileJpaEntity entity = repository.save(new ProfileJpaEntity("36", "Front", null, true));
        assertThat(entity.getId()).isNotNull();
    }
}
```

Web:

```java
@WebMvcTest(ProfileRestController.class)
class ProfileRestControllerTest {
    @Autowired MockMvc mvc;
    @MockBean CreateProfileUseCase createProfile;

    @Test
    void validatesCreateRequest() throws Exception {
        mvc.perform(post("/api/profiles").contentType(APPLICATION_JSON).content("{}"))
                .andExpect(status().isBadRequest());
    }
}
```

## ArchUnit Boundary Rules

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
    static final ArchRule domain_must_not_depend_on_spring_or_adapters =
            noClasses().that().resideInAPackage("..domain..")
                    .should().dependOnClassesThat().resideInAnyPackage(
                            "org.springframework..",
                            "jakarta.persistence..",
                            "..adapter.."
                    );

    @ArchTest
    static final ArchRule application_must_not_depend_on_adapters =
            noClasses().that().resideInAPackage("..application..")
                    .should().dependOnClassesThat().resideInAPackage("..adapter..");

    @ArchTest
    static final ArchRule inbound_adapters_must_not_depend_on_outbound_adapters =
            noClasses().that().resideInAPackage("..adapter.in..")
                    .should().dependOnClassesThat().resideInAPackage("..adapter.out..");
}
```

Run order: domain tests, use case tests, adapter tests, integration tests.
