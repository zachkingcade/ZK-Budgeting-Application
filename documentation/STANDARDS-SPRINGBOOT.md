# Core Philosophy

This document defines coding, structure, and design standards for this Java Spring Boot project.

- Code should be **easy to read first, efficient second**
- Prefer **explicit over implicit**
- Prefer **simple over abstract**
- Avoid premature optimization
- Avoid unnecessary complexity
- Do not mix responsibilities across layers
- Only introduce unnecessary abstractions in service of following the architecture design patterns


# Naming Conventions

## General Rule

Use **descriptive, readable names**.

Bad:
- `data`
- `obj`
- `thing`

Good:
- `accountBalance`
- `journalEntryList`
- `selectedAccountTypeIds`

## Variables & Members

Use **camelCase**

```java
accountBalance
selectedAccountIds
isActive
```

## Methods

Use **camelCase**, with **verb-first naming**

Format:
```
verb + subject
```

Examples:

```java
getAccounts()
createJournalEntry()
updateAccountBalance()
applyFilters()
```

Avoid vague method names:
- `handleData()`
- `process()`



## Classes

Use **PascalCase**

```java
AccountService
LedgerEntryController
AccountRepository
```



## Interfaces

Use **PascalCase**, no prefix

```java
AccountRepository
AccountService
```

Do NOT use `IAccountService`, this is a javascript standard and is used in the javascript parts of this project but not in the java sections.



## Constants

Use **UPPER_SNAKE_CASE**

```java
MAX_PAGE_SIZE
DEFAULT_SORT_DIRECTION
```



## Packages

Use **lowercase**, dot-separated

```java
com.project.ledger.account
com.project.ledger.journal
```

## Usecases/Commands

Use format **Command** + **Subject** + **Type**

```java
GetAllAccountsCommand
GetByIdAccountTypesUsecase
```



# Project Structure

Use a **layered architecture**

```
adapter/
application/
domain/
```



## Layer Responsibilities

### adapter

- Handles external-facing concerns (HTTP, persistence, configuration)
- Maps request → application command / use case call
- Maps application result → response DTO (never return entities directly)
- Owns API request/response models
- May perform **non-business** enrichment such as:
  - adding display names
  - calculating and returning execution time in `MetaData`

Rules:
- No domain/business rules (validation and invariants live in `domain/`)
- Keep methods small
- Temporary exception: some existing endpoints use `GET` + `@RequestBody` for complex filtering. Keep these as-is until we migrate them; do not add new endpoints following this pattern.

### Application

- Contains use cases
- Orchestrates domain + repository calls
- Everything is handled using dependency inversion

Rules:
- Use cases and ports are **interfaces only** (no Spring stereotypes like `@Service` on interfaces)
- Prefer domain objects and command objects as inputs/outputs (avoid leaking web DTOs into application)


### Domain

- Core business logic
- Validation rules
- Pure logic (No Spring annotations, Pure java)
- Eliminates invalid data



# API Design

## Response Structure

All responses should be consistent.
Example:

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



# Business Logic Rules

- Business rules belong in:
  - Domain layer

- Never place business logic in:
  - Controllers
  - Entities
  - Repositories



# Data Handling

- Always validate input at domain layer
- Avoid trusting client data
- Use DTOs for all external communication

# DTO Standards

- Prefer Java `record` for immutable request/response DTOs.
- Handle defaults at the adapter/application boundary.


# Ports / Adapters Boundary (Pragmatic)

This service is **pragmatic**, not “pure clean architecture”.

Rules:
- Application ports must not reference **web DTOs**.
- Avoid Spring stereotypes (e.g. `@Service`) on **interfaces** (ports/use cases). Annotate implementations instead.
- Prefer keeping persistence details in `adapter/out/persistence/*`. If Spring Data types (`Sort`, `Specification`) are used across the boundary, keep it consistent and document it in the relevant port.


# Code Cleanliness

- Remove unused imports
- Remove commented-out code
- Keep methods short and focused
- Avoid deep nesting



# Anti-Patterns

Avoid:

- Fat controllers
- Business logic in controllers
- Returning entities directly from APIs
- Massive service classes
- Generic utility classes like `Utils.java`
- Overuse of inheritance



# Testing Philosophy

- Focus on:
  - service logic
  - domain rules

- Avoid over-testing:
  - trivial mappings
  - simple getters/setters

# Exception Handling

- Use centralized exception handling (`@ControllerAdvice`)
- Do not expose raw exceptions to clients
- Return meaningful error messages

## Error Responses

All error responses should use a consistent shape.

```json
{
  "errorCode": "DOMAIN_ERROR",
  "message": "Human readable message"
}
```


# Logging

Logging exists to make production debugging easy.

Rules:
- Log at the **start** and **end** of major functions (controller endpoints, application service use cases, persistence adapter entrypoints).
- Log when functions **error out** (log the exception with stack trace).
- Prefer `debug` for start/end, and `error` for failures.
- Include **business identifiers** and **counts** in logs (IDs, sizes), not whole objects.
- Do not log secrets/credentials or large payloads.

Example:

```java
log.debug("Starting updateAccount id:[{}] descriptionPresent:[{}]", command.id(), command.description().isPresent());
try {
    // ...
    log.debug("Ending updateAccount updatedId:[{}]", saved.id());
    return saved;
} catch (RuntimeException ex) {
    log.error("updateAccount failed for id:[{}]", command.id(), ex);
    throw ex;
}
```


# Something not covered?

Choose the option that is easiest to understand and maintain later.
