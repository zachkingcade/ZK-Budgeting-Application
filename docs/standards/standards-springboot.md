Purpose: Define coding, structure, and design standards for Spring Boot services in this repository.
Last updated: 2026-04-22

# Core Philosophy

This document defines coding, structure, and design standards for Java Spring Boot services in this repo.

- Code should be **easy to read first, efficient second**
- Prefer **explicit over implicit**
- Prefer **simple over abstract**
- Avoid premature optimization
- Avoid unnecessary complexity
- Do not mix responsibilities across layers
- Only introduce abstractions when they serve the architecture

## Naming Conventions

### General Rule

Use **descriptive, readable names**.

### Variables & Members

Use **camelCase**.

### Methods

Use **camelCase**, with **verb-first naming**.

### Classes

Use **PascalCase**.

### Interfaces

Use **PascalCase**, no prefix in Java.

### Constants

Use **UPPER_SNAKE_CASE**.

### Packages

Use **lowercase**, dot-separated.

### Usecases/Commands

Use format **Command** + **Subject** + **Type**.

## Project Structure

Use a **layered architecture**:

```
adapter/
application/
domain/
```

### Layer Responsibilities

#### adapter

- HTTP/persistence/config concerns
- Map request → command/use case call
- Map application result → response DTO (never return entities directly)

Rules:
- No domain/business rules in adapters
- Keep methods small

#### application

- Use cases
- Orchestrate domain + repository calls
- Dependency inversion via ports

Rules:
- Ports/use cases are interfaces only (no Spring stereotypes on interfaces)
- Do not leak web DTOs into application layer

#### domain

- Core business logic and invariants
- Pure Java (no Spring annotations)

## API Design

### Response Structure

Success responses should follow a consistent envelope:

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

## Business Logic Rules

- Business rules belong in `domain/`.
- Do not place business logic in controllers/entities/repositories.

## DTO Standards

- Prefer Java `record` for immutable request/response DTOs.
- Handle defaults at the adapter/application boundary.

## Code Cleanliness

- Remove unused imports and dead commented-out code
- Keep methods short and focused
- Avoid deep nesting

## Anti-Patterns

Avoid:

- Fat controllers
- Business logic in controllers
- Returning entities directly from APIs
- Massive service classes
- Generic utility classes like `Utils.java`

## Testing Philosophy

- Focus on service logic and domain rules
- Avoid over-testing trivial mappings and obvious getters/setters

## Exception Handling

- Use centralized exception handling (e.g. `@ControllerAdvice`)
- Do not expose raw exceptions to clients
- Return meaningful error messages

## Logging

Logging exists to make production debugging easy.

- Log start/end of major operations at `debug`
- Log failures at `error` with stack traces
- Log identifiers and counts, not secrets or large payloads

## Something not covered?

Choose the option that is easiest to understand and maintain later.

