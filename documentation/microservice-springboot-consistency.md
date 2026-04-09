# Spring Boot microservice consistency

This document was written shortly after [ledger-service](../ledger-service) and [user-service](../user-service) reached their current shapes. Its purpose is to to align future Spring Boot microservices with the patterns, API contracts, and layering already in use—so new services do not ad-hoc their structure or drift accidentally from the pattern.

## Canonical package layout (new services)

For **new** microservices, use **flat adapter packages** and keep Spring `@Configuration` next to the web layer:

| Layer | Package | Contents |
|--------|-----------------------------------------------|----------|
| Web adapter | `adapter/web` | REST controllers, web DTOs (adapter/web/dto/...), ApiResponse, MetaData, GlobalExceptionHandler, RequestTimingFilter, ApiResponseMetaAdvice |
| Web config | `adapter/web/config` | SecurityConfig, CORS, JWT decoder (when using resource server), other web-facing beans |
| Persistence adapter | `adapter/persistence` | jpa/*Entity, repository/*JpaRepository, *PersistenceAdapter implementing outbound ports |
| Application | `application` | *Service implementations, commands/, port/in/**, port/out/**, results/ (optional) |
| Domain | `domain` | Domain models, domain/exception as needed |


## Core philosophy

- Code should be **easy to read first, efficient second**
- Prefer **explicit over implicit** and **simple over abstract**
- Avoid premature optimization and unnecessary complexity
- Do not mix responsibilities across layers
- Introduce single use abstractions only when they serve the architecture


## Naming conventions

### General rule

Use **descriptive, readable names** (for example `accountBalance`, not `data`).

### Variables and members

Use **camelCase**.

### Methods

Use **camelCase**, **verb-first**: `getAccounts()`, `createJournalEntry()`, `updateAccountBalance()`. Avoid vague names like `handleData()` or `process()`.

### Classes

Use **PascalCase** (for example `AccountService`, `AccountController`).

### Interfaces

Use **PascalCase**, **no `I` prefix** in Java (for example `AccountRepositoryPort`, `CreateAccountUseCase`). The `I` prefix is a **TypeScript** convention in this repo ([user-interface-service/STANDARDS-ANGULAR.md](../user-interface-service/STANDARDS-ANGULAR.md)); do not use it on Java ports.

### Constants

Use **UPPER_SNAKE_CASE`.

### Packages

Use **lowercase**, dot-separated (for example `zachkingcade.dev.payments.adapter.web`).

### Commands

Prefer names that read as **action + subject** (for example `CreateAccountCommand`, `GetByIdAccountCommand`, `GetAllJournalEntriesCommand`).

---

## Layered architecture

Use three top-level layers: `adapter`, `application`, `domain`.

### Adapter

- Handles HTTP, persistence wiring, and other infrastructure-facing code.
- Maps HTTP requests → application commands / use case calls.
- Maps application results → **response DTOs** (never expose JPA entities from controllers).
- Owns API request/response models.
- May perform **non-business** enrichment (for example filling `MetaData`, including `executionTimeMs` via filters/advice).

**Rules:**

- No domain/business rules in adapters; validation and invariants belong in `domain` (and orchestration in `application`).
- Keep controller methods small.
- **Do not add new endpoints** that use `GET` with `@RequestBody` for complex filtering. Some existing ledger endpoints still do this; leave them until migrated.

### Application

- Implements use cases; orchestrates domain and repository ports.
- Dependency inversion: depend on `port/in` and `port/out` abstractions.

**Rules:**

- Use case and port types are **interfaces only**—do not put `@Service` (or other Spring stereotypes) on interfaces. Annotate **implementations** (typically `*Service`, `*PersistenceAdapter`).
- Prefer domain objects and command objects across the application boundary; **do not** reference web DTOs from `application`.

### Domain

- Core business rules, validation, and invariants.
- **Pure Java**: no Spring annotations in domain types.

---

## API design

### Success envelope

All successful JSON responses should follow the same shape (implemented via `ApiResponse<T>` and `MetaData` in both existing services):

```json
{
  "statusMessage": "Returned [10] Accounts",
  "metaData": {
    "requestDate": "2026-03-31",
    "requestTime": "12:34:56.789",
    "executionTimeMs": 12,
    "dataResponseCount": 10
  },
  "data": {}
}
```

**Implementation pattern:** `RequestTimingFilter` records start time; `ApiResponseMetaAdvice` enriches `ApiResponse` instances with timestamps and execution time where applicable.

### Error envelope

Use a dedicated error DTO (for example `ApiErrorResponse`) and centralized handling with `@RestControllerAdvice`:

```json
{
  "errorCode": "DOMAIN_ERROR",
  "message": "Human readable message"
}
```

Map domain, application, not-found, bad request, and unexpected errors to appropriate HTTP status codes without leaking raw stack traces to clients.

---

## Business logic and data handling

- Business rules belong in the **domain** layer.
- Do not use controllers, JPA entities, or repositories as the **authority** for business rules.
- Validate and enforce invariants in the domain; treat client input as untrusted.
- Use DTOs for all external (HTTP) communication.

### DTO standards

- Prefer Java `record` for immutable request/response DTOs.
- Apply defaults at the adapter/application boundary, not deep in the domain.

---

## Ports and adapters

- **Inbound:** `application/port/in/**` defines use cases; controllers depend only on those interfaces.
- **Outbound:** `application/port/out/**` defines `*RepositoryPort` (or similar); `*PersistenceAdapter` in `adapter/persistence` implements them and maps **JPA entities ↔ domain**.

If Spring Data types (`Sort`, `Specification`) or JPA entity types appear in the application layer, treat that as an **explicit trade-off**: keep it consistent, document it on the relevant port or service, and prefer pushing complexity into the persistence adapter when it stays understandable.

---

## Code cleanliness, anti-patterns, testing

### Code cleanliness

- Remove unused imports and dead commented-out code.
- Keep methods short; avoid deep nesting.

### Anti-patterns

Avoid fat controllers, business logic in controllers, returning entities from APIs, oversized service classes, grab-bag `Utils.java`, and deep inheritance hierarchies without need.

### Testing

- Focus tests on application services and domain rules.
- Avoid over-testing trivial DTO mapping and obvious getters/setters.

---

## Logging

Logging should support production debugging.

- Log **start** and **end** of major operations (controllers, application services, persistence adapter entrypoints) at **debug**.
- Log failures at **error** with the exception (stack trace).
- Log **identifiers and counts**, not full payloads or secrets.

---

## Build and runtime baseline

### Java and Spring Boot

- **Java 17**
- **Spring Boot 4.x** (`spring-boot-starter-parent`)

### Typical dependencies (domain microservices)

- `spring-boot-starter-webmvc`
- `spring-boot-starter-data-jpa`
- `spring-boot-starter-flyway`
- `spring-boot-starter-validation`
- PostgreSQL driver (runtime)
- `flyway-database-postgresql`
- Lombok (optional)

### Database: schema per service

Both services use a **shared PostgreSQL database** with a **dedicated schema** per service (for example `ledger`, `auth`), configured similarly in `application.yml`:

- JDBC URL with `currentSchema=<schema>`
- Hibernate `default_schema`
- Flyway `schemas` and `default-schema`
- `spring.jpa.hibernate.ddl-auto: validate`
- `spring.autoconfigure.exclude` listing DataSource, Hibernate JPA, and Flyway auto-configuration

### Server ports

Assign a **unique** `server.port` per service (ledger: **8081**, user: **8082**). Document new ports in this file or in deployment docs when you add services.

### CORS

Browser clients (for example Angular on port 4200) need explicit CORS when calling APIs directly. Use a **service-scoped** property, for example:

```yaml
<service-short-name>:
  cors:
    allowed-origins: "http://localhost:4200,http://127.0.0.1:4200"
```

Wire `@Value("${<service-short-name>.cors.allowed-origins:...}")` in your CORS configuration.

### Flyway

Place SQL under `src/main/resources/db/migration/` using versioned names: `V1__Init.sql`, `V2__...`, etc.

### Logging files

Both services use `src/main/resources/logback-spring.xml` with a configurable `APP_NAME` and rolling file appenders. New services should copy this pattern and set `APP_NAME` to the real service name.

---

## Security patterns (intentional differences)

Do not force one security model on every service. The two existing services split responsibilities by design.

| Concern | ledger-service | user-service |
|--------|----------------|--------------|
| Primary role | Protected **domain API** | **Identity / session** edge |
| API auth | OAuth2 **resource server**, JWT validation (`JwtDecoderConfig`), `@AuthenticationPrincipal Jwt` on controllers | Permits defined `POST` routes under `/user/**`; `httpBasic` for other requests; session tokens stored in DB |
| Tokens | Validates access tokens issued elsewhere | JJWT for access tokens; custom session handling |
| Password hashing | Not applicable | `BCryptPasswordEncoder` bean |
| Actuator | Included | Not included |

**Guidance for new services:**

- New **business / domain** APIs (similar to ledger) should default to **OAuth2 resource server + JWT**, consistent with ledger.

---

## Relationship to the Angular / UI standards

[user-interface-service/STANDARDS-ANGULAR.md](../user-interface-service/STANDARDS-ANGULAR.md) defines the front-end stack. Backend microservices should stay compatible with these expectations:

- Keep **stable JSON contracts**: success envelope (`statusMessage`, `metaData`, `data`) and error envelope (`errorCode`, `message`) so the UI can handle responses and failures predictably.
- The UI should not scatter raw API shapes everywhere; correspondingly, **avoid unnecessary breaking changes** to field names and envelope structure without coordination.

---

## New microservice checklist

Use this when adding another Spring Boot service to the repo:

- [ ] `pom.xml`: Java 17, Spring Boot parent aligned with sibling services where possible; web, JPA, Flyway, validation, PostgreSQL + Flyway PostgreSQL support
- [ ] Base package `zachkingcade.dev.<service>`
- [ ] `adapter/web`, `adapter/persistence`, `adapter/web/config` (canonical layout)
- [ ] `ApiResponse`, `MetaData`, `RequestTimingFilter`, `ApiResponseMetaAdvice`, `ApiErrorResponse`, `GlobalExceptionHandler`
- [ ] `application.yml`: schema-per-service JDBC settings, unique `server.port`, **correct** `spring.application.name`, service-scoped CORS property
- [ ] `logback-spring.xml` with correct `APP_NAME`
- [ ] Flyway `V1__...sql` for initial schema
- [ ] `port/in` use cases + `port/out` repository ports + `*Service` + `*PersistenceAdapter`
- [ ] Security model resource server

---

## When something is not covered here

Choose the option that is **easiest to understand and maintain later**, and consider updating this document when a new pattern repeats across services.
