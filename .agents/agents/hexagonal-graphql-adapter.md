---
name: hexagonal-graphql-adapter
description: Use when adding or reviewing GraphQL schema-first adapters under adapter/in/web/graphql, including .graphqls files, resolvers, DataLoader/BatchMapping, GraphQL errors, and MCP schema synchronization.
---

# Hexagonal GraphQL Adapter

GraphQL is an inbound web adapter. It calls the same domain input ports as REST.

## Package And Schema Location

```text
src/main/java/com/empresa/app/<module>/adapter/in/web/graphql
src/main/resources/graphql/<module>.graphqls
```

## Wrong: Resolver Uses Repository

```java
package com.empresa.app.profiles.adapter.in.web.graphql;

@Controller
class ProfileGraphqlController {
    private final SpringDataProfileRepository repository;

    @QueryMapping
    ProfileJpaEntity profile(@Argument Long id) {
        return repository.findById(id).orElseThrow();
    }
}
```

Bug: GraphQL bypasses use cases and exposes persistence fields to clients.

## Correct Schema

```graphql
type Query {
  profile(id: ID!): Profile
  profiles: [Profile!]!
}

type Mutation {
  createProfile(input: CreateProfileInput!): Profile!
}

input CreateProfileInput {
  code: String!
  name: String!
  description: String
}

type Profile {
  id: ID!
  code: String!
  name: String!
  description: String
  active: Boolean!
}
```

## Correct Resolver

```java
package com.empresa.app.profiles.adapter.in.web.graphql;

import com.empresa.app.profiles.domain.port.in.CreateProfileUseCase;
import com.empresa.app.profiles.domain.port.in.GetProfileUseCase;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;

@Controller
class ProfileGraphqlController {
    private final GetProfileUseCase getProfile;
    private final CreateProfileUseCase createProfile;
    private final ProfileGraphqlMapper mapper;

    ProfileGraphqlController(GetProfileUseCase getProfile, CreateProfileUseCase createProfile,
                             ProfileGraphqlMapper mapper) {
        this.getProfile = getProfile;
        this.createProfile = createProfile;
        this.mapper = mapper;
    }

    @QueryMapping
    ProfilePayload profile(@Argument Long id) {
        return mapper.toPayload(getProfile.findById(id));
    }

    @MutationMapping
    ProfilePayload createProfile(@Argument CreateProfileInput input) {
        return mapper.toPayload(createProfile.create(mapper.toCommand(input)));
    }
}
```

## DataLoader / BatchMapping For N+1

Wrong:

```java
@SchemaMapping
InventoryPayload inventory(CuttingPayload cutting) {
    return inventoryRepository.findById(cutting.inventoryItemId()).map(mapper::toPayload).orElse(null);
}
```

Bug: one inventory query per cutting row.

Correct:

```java
@BatchMapping(typeName = "CuttingRecord", field = "inventory")
Map<CuttingPayload, InventoryPayload> inventory(List<CuttingPayload> cuttings) {
    List<Long> ids = cuttings.stream().map(CuttingPayload::inventoryItemId).toList();
    Map<Long, InventoryPayload> inventoryById = loadInventory.loadAll(ids);
    return cuttings.stream().collect(Collectors.toMap(c -> c, c -> inventoryById.get(c.inventoryItemId())));
}
```

## GraphQL Error Handling

REST errors use `GlobalExceptionHandler`; GraphQL errors should use a GraphQL exception resolver so stack traces and SQL details do not leak.

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

## MCP Rule

The GraphQL schema is the stable contract MCP should summarize first: types, queries, mutations, resolver classes, and input ports. MCP narrows context; it does not replace code review.
