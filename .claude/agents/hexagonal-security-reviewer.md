---
name: hexagonal-security-reviewer
description: Use after any change touching auth, JWT, SecurityFilterChain, HTTP interceptors, Angular guards, password handling, or database queries. Mandatory before merging — not optional. Reviews both tesla-api and tesla-web-app security boundaries.
---

# Hexagonal Security Reviewer

Security infrastructure is an adapter. Domain must not import Spring Security.

## SecurityFilterChain Is Infrastructure

Correct package:

```java
package com.empresa.app.security.adapter.in.web;

@Configuration
class SecurityConfiguration {
    @Bean
    SecurityFilterChain security(HttpSecurity http, JwtAuthenticationFilter jwt) throws Exception {
        return http
                .csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/health").permitAll()
                        .anyRequest().authenticated()
                )
                .addFilterBefore(jwt, UsernamePasswordAuthenticationFilter.class)
                .build();
    }
}
```

Wrong:

```java
package com.empresa.app.cutting.domain.model;

import org.springframework.security.core.Authentication;

public final class CuttingRecord {
    private Authentication operator;
}
```

Bug: domain becomes tied to web authentication and cannot be tested or reused without Spring Security.

## JWT Filter As Inbound Adapter

```java
package com.empresa.app.security.adapter.in.web;

class JwtAuthenticationFilter extends OncePerRequestFilter {
    private final ValidateTokenUseCase validateToken;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain chain) throws IOException, ServletException {
        String token = resolveBearerToken(request);
        if (token != null) {
            CurrentUser user = validateToken.validate(token);
            SecurityContextHolder.getContext().setAuthentication(toAuthentication(user));
        }
        chain.doFilter(request, response);
    }
}
```

## Do Not Trust Operator IDs From Request Bodies

Wrong:

```java
public record CreateCuttingRequest(Long operatorId, Long inventoryItemId, int initialQuantity) {
}
```

Bug: client can impersonate another operator and corrupt traceability.

Correct:

```java
@PostMapping("/api/cutting")
CuttingResponse create(@AuthenticationPrincipal CurrentUser user,
                       @Valid @RequestBody CreateCuttingRequest request) {
    CreateCuttingCommand command = new CreateCuttingCommand(
            request.inventoryItemId(),
            request.machineId(),
            user.id(),
            request.shiftId(),
            request.initialQuantity(),
            request.goodQuantity(),
            request.scrapQuantity()
    );
    return mapper.toResponse(createCutting.create(command));
}
```

## Authorization Placement

- Adapter-level authorization is good for coarse route access: authenticated, role required, endpoint disabled.
- Use case authorization is preferred for business actions that must apply to REST, GraphQL, batch jobs, and future adapters.

Correct use case check:

```java
@Transactional
public CuttingResult create(CreateCuttingCommand command) {
    permissions.requireAny(command.operatorId(), "ROLE_OPERATOR", "ROLE_SUPERVISOR");
    return CuttingResultMapper.toResult(saveCutting.save(CuttingRecord.record(...)));
}
```

Do not log passwords, JWTs, refresh tokens, or password hashes.
