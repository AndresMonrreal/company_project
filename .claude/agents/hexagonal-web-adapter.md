---
name: hexagonal-web-adapter
description: Use when implementing REST controllers, request/response DTOs, validation, web mappers, HTTP status semantics, and inbound web adapters that call domain input ports.
---

# Hexagonal Web Adapter

REST controllers live in `adapter/in/web`. They bind HTTP to input ports and return web DTOs.

## Wrong: Controller Talks To Repository

```java
package com.empresa.app.profiles.adapter.in.web;

@RestController
class ProfileController {
    private final SpringDataProfileRepository repository;

    @PostMapping("/api/profiles")
    ProfileJpaEntity create(@RequestBody ProfileCreateRequest request) {
        return repository.save(new ProfileJpaEntity(request.code(), request.name()));
    }
}
```

Bug: HTTP bypasses use case validation, transactions, and domain rules; response exposes persistence fields.

## Correct: Controller Calls Input Port

```java
package com.empresa.app.profiles.adapter.in.web;

@RestController
@RequestMapping("/api/profiles")
class ProfileRestController {
    private final CreateProfileUseCase createProfile;
    private final ProfileWebMapper mapper;

    ProfileRestController(CreateProfileUseCase createProfile, ProfileWebMapper mapper) {
        this.createProfile = createProfile;
        this.mapper = mapper;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    ProfileResponse create(@Valid @RequestBody ProfileCreateRequest request) {
        return mapper.toResponse(createProfile.create(mapper.toCommand(request)));
    }
}
```

## Request DTO

```java
package com.empresa.app.profiles.adapter.in.web.dto;

public record ProfileCreateRequest(
        @NotBlank @Size(max = 10) String code,
        @NotBlank @Size(max = 100) String name,
        @Size(max = 255) String description
) {
}
```

## Response DTO

```java
package com.empresa.app.profiles.adapter.in.web.dto;

public record ProfileResponse(Long id, String code, String name, String description, boolean active) {
}
```

## Mapper

```java
package com.empresa.app.profiles.adapter.in.web;

class ProfileWebMapper {
    CreateProfileCommand toCommand(ProfileCreateRequest request) {
        return new CreateProfileCommand(request.code(), request.name(), request.description());
    }

    ProfileResponse toResponse(ProfileResult result) {
        return new ProfileResponse(result.id(), result.code(), result.name(), result.description(), result.active());
    }
}
```

## HTTP Status Semantics

- `200 OK`: successful read/update with body.
- `201 CREATED`: successful create.
- `204 NO_CONTENT`: successful delete/deactivate with no body.
- `400 BAD_REQUEST`: malformed request or bean validation failure.
- `404 NOT_FOUND`: domain object does not exist.
- `409 CONFLICT`: duplicate profile code or state conflict.
- `422 UNPROCESSABLE_ENTITY`: valid request syntax, failed business rule.

Do not put business rules in controllers. Validate shape here; enforce business invariants in domain.
