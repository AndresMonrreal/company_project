---
description: Run tests in hexagonal order and report coverage by layer
argument-hint: [module-or-test-pattern]
---

# Run Tests

Input: `$ARGUMENTS`

## Order

1. Domain tests.
2. Use case tests.
3. Adapter slice tests.
4. ArchUnit boundary tests.
5. Integration tests.

## Commands

Compile first:

```powershell
.\gradlew.bat compileJava testClasses
```

Run targeted tests:

```powershell
.\gradlew.bat test --tests "*CuttingQuantitiesTest"
.\gradlew.bat test --tests "*CreateProfileServiceTest"
.\gradlew.bat test --tests "*HexagonalArchitectureTest"
```

Run all tests when DB/config is ready:

```powershell
.\gradlew.bat test
```

## Output

```text
Domain tests:
Use case tests:
Adapter tests:
ArchUnit boundary tests:
Integration tests:
Untested changed paths:
Result:
```
