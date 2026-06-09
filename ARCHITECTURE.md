# Spring Boot Project Structure

This project uses a feature-based Spring Boot structure. Each business area gets its own package so the code stays close to the process it represents.

## Root Packages

- `config`: application-wide Spring configuration, such as CORS, beans, object mapping, or future environment setup.
- `security`: authentication and authorization configuration, such as JWT filters, password encoding, and role rules.
- `common`: reusable utilities, enums, constants, base DTOs, and shared helpers used by multiple modules.
- `exception`: global exception handling and shared API error responses.

## Business Modules

- `auth`: login, token creation, and session-related logic.
- `users`: system users, operators, supervisors, admins, and report-only users.
- `roles`: role and permission definitions.
- `profiles`: product profiles such as 36 front, 37 rear, 38 front, and 39 rear con liga.
- `containers`: scanned physical containers such as tinas and carritos.
- `machines`: cutting machines such as Corte 1, Corte 2, and Corte 3.
- `shifts`: work shifts used for production traceability.
- `movementtypes`: catalog of movement types used in the material history.
- `reception`: material reception, where inventory starts.
- `inventory`: available material that can be sent to cutting.
- `cutting`: cutting records and the main rule: initial quantity equals good quantity plus scrap.
- `scrap`: scrap/merma details created from the cutting process.
- `molding`: output to molding, normally equal to the good cut quantity.
- `traceability`: complete container and lot history.
- `reports`: report queries and summaries.
- `exports`: Excel/PDF exports for reports.

## Standard Module Layers

Each main module can contain these folders:

- `controller`: HTTP endpoints. This is where requests enter the API.
- `dto`: request and response objects. This keeps API data separate from database entities.
- `entity`: JPA/Hibernate database models.
- `repository`: Spring Data JPA database access.
- `service`: business rules and process logic.
- `mapper`: conversion between entities and DTOs.
- `exception`: module-specific errors.

## Resources

- `src/main/resources/application.properties`: Spring Boot configuration.
- `src/main/resources/db/migration`: future Flyway migration files.
- `src/main/resources/static`: optional static files.
- `src/main/resources/templates`: optional server-rendered templates or export templates.

No runtime behavior was added with this structure. The created packages are placeholders for the next implementation steps.
