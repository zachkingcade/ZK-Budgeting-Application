Purpose: Define coding, structure, and design standards for Angular/TypeScript in this repository.
Last updated: 2026-04-22

# Angular / TypeScript Standards

This document defines coding, structure, and design standards for this Angular / TypeScript project.

- Code should be **easy to read first, efficient second**
- Prefer **explicit over implicit**
- Prefer **simple over abstract**
- Avoid premature optimization
- Avoid unnecessary complexity
- Do not mix responsibilities across layers
- Only introduce unnecessary abstractions in service of following the architecture design patterns

## Naming Conventions

### General Rule

Use **descriptive, readable names**.

Bad:
- `data`
- `obj`
- `thing`

Good:
- `accountBalance`
- `journalEntryList`
- `selectedAccountTypeIds`

### Variables & Members

Use **camelCase**

```ts
accountBalance
selectedAccountIds
isActive
```

### Methods

Use **camelCase**, with **verb-first naming**

Format:
```
verb + subject
```

Examples:

```ts
getAccounts()
createJournalEntry()
updateAccountBalance()
applyFilters()
```

Avoid vague method names:
- `handleData()`
- `process()`

### Classes

Use **PascalCase**

```ts
AccountService
LedgerPageComponent
AccountRepository
```

### Interfaces

Use **PascalCase**, prefixed with `I`

```ts
IAccount
IJournalEntry
IAccountType
```

Use the `I` prefix for interfaces in the TypeScript sections of this project.

### Constants

Use **UPPER_SNAKE_CASE**

```ts
MAX_PAGE_SIZE
DEFAULT_SORT_DIRECTION
```

### Files

Use Angular standard **dot** and **kebab-case** for file names.

```txt
ledger-page.component.ts
account-type-form.component.ts
account-api.service.ts
```

## Project Structure

Use a **layered architecture**

```txt
presentation/
application/
domain/
adapter/
```

### Layer Responsibilities

#### Presentation

- Angular components
- HTML templates
- SCSS/CSS styles
- user interaction
- display state only

Rules:
- No business logic
- No direct HTTP calls
- Keep components small and focused

#### Application

- Contains use cases
- Orchestrates domain + infrastructure calls
- Everything is handled using dependency inversion

#### Domain

- UI domain models (keep “dummy UI” as much as possible; business rules belong in backend services)

#### adapter

- HTTP/API calls
- DTO mapping
- external integrations
- repository implementations

## UI Design

### Component Rules

- Keep components focused on one purpose
- Prefer composition over large all-in-one components
- Extract reusable display pieces when it improves clarity
- Do not create extra components for trivial markup

### Page Rules

- Pages coordinate filters, application calls, and display state
- Pages should not contain business rules

### Forms

- Prefer explicit field names over overly generic models
- Validation messaging should be clear and predictable

## Data Handling

- Avoid trusting client-side input
- Use DTOs for all external communication
- Do not let raw API shapes leak throughout the UI
- Map external DTOs into UI models before broad use

## Code Cleanliness

- Remove unused imports
- Remove commented-out code
- Keep methods short and focused
- Avoid deep nesting

### Dependency Injection

- Prefer constructor-based DI over `inject()`

### Typing

- Always type new variables explicitly.
- If unknown, use `any`; if multiple possible types, use a union.

## Anti-Patterns

Avoid:

- Massive components
- Business logic in components
- Direct HTTP calls in components
- Generic utility files like `utils.ts`
- Overuse of inheritance
- Use of global state

## Testing Philosophy

- Focus on important component behavior and application logic
- Avoid over-testing trivial mappings and obvious getters/setters

## Error Handling

- Handle API failures predictably
- Do not silently swallow errors
- Keep user-facing error messages clear and simple

## Something not covered?

Choose the option that is easiest to understand and maintain later.

