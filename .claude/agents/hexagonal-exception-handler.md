---
name: hexagonal-exception-handler
description: Use when implementing domain exception hierarchy, GlobalExceptionHandler, REST ApiErrorResponse mapping, validation error collection, and GraphQL DataFetcherExceptionResolver in tesla-api.
---

# Hexagonal Exception Handler

Domain exceptions are pure Java. Adapters translate them to REST or GraphQL responses.

## Domain Base

```java
package com.empresa.app.shared.domain.exception;

public abstract class DomainException extends RuntimeException {
    private final DomainErrorType type;
    private final String code;

    protected DomainException(DomainErrorType type, String code, String message) {
        super(message);
        this.type = type;
        this.code = code;
    }
}
```

Wrong:

```java
public class ProfileNotFoundException extends ResponseStatusException {
    public ProfileNotFoundException(Long id) {
        super(HttpStatus.NOT_FOUND, "Profile not found " + id);
    }
}
```

Bug: domain now imports HTTP concerns and cannot be reused by GraphQL or jobs.

Correct:

```java
public class ProfileNotFoundException extends DomainException {
    public ProfileNotFoundException(Long id) {
        super(DomainErrorType.NOT_FOUND, "profile.not-found", "Profile not found with id: " + id);
    }
}
```

## REST Error Format

```java
public record ApiErrorResponse(
        LocalDateTime timestamp,
        int status,
        String error,
        String message,
        String path,
        Map<String, String> fieldErrors
) {
}
```

Correct REST handler:

```java
@RestControllerAdvice
class GlobalExceptionHandler {
    @ExceptionHandler(DomainException.class)
    ResponseEntity<ApiErrorResponse> domain(DomainException ex, HttpServletRequest request) {
        HttpStatus status = switch (ex.type()) {
            case NOT_FOUND -> HttpStatus.NOT_FOUND;
            case CONFLICT -> HttpStatus.CONFLICT;
            case BUSINESS_RULE -> HttpStatus.UNPROCESSABLE_ENTITY;
        };
        return build(status, ex.getMessage(), request.getRequestURI(), Map.of());
    }
}
```

Wrong validation:

```java
String message = ex.getBindingResult().getFieldErrors().get(0).getDefaultMessage();
```

Bug: clients see one invalid field at a time.

Correct validation:

```java
Map<String, String> fieldErrors = new LinkedHashMap<>();
ex.getBindingResult().getFieldErrors().forEach(error ->
        fieldErrors.putIfAbsent(error.getField(), error.getDefaultMessage())
);
```

## GraphQL Errors

GraphQL must use a resolver, not REST advice:

```java
@Component
class DomainGraphqlExceptionResolver extends DataFetcherExceptionResolverAdapter {
    @Override
    protected GraphQLError resolveToSingleError(Throwable ex, DataFetchingEnvironment env) {
        if (ex instanceof DomainException domainException) {
            return GraphqlErrorBuilder.newError(env)
                    .message(domainException.getMessage())
                    .errorType(ErrorType.BAD_REQUEST)
                    .build();
        }
        return null;
    }
}
```
