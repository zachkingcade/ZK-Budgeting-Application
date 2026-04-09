# Angular / TypeScript Standards

This document defines coding, structure, and design standards for this Angular / TypeScript project.

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

```ts
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

```ts
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

```ts
AccountService
LedgerPageComponent
AccountRepository
```

## Interfaces

Use **PascalCase**, prefixed with `I`

```ts
IAccount
IJournalEntry
IAccountType
```

Use the `I` prefix for interfaces in the TypeScript sections of this project. This is a project standard and should be followed consistently.

## Constants

Use **UPPER_SNAKE_CASE**

```ts
MAX_PAGE_SIZE
DEFAULT_SORT_DIRECTION
```

## Files

Use angular standard **dot** and **kebab-case** for file names.

```txt
ledger-page.component.ts
account-type-form.component.ts
account-api.service.ts
```

Component, service, model, and utility file names should clearly describe what they contain.

## Selectors

Use the Angular standard `app-` prefix and **kebab-case**

```html
<app-ledger-page></app-ledger-page>
<app-account-table></app-account-table>
```

---

# Project Structure

Use a **layered architecture**

```txt
presentation/
application/
domain/
adapter/
```

A layer-first structure is preferred. Organize files by layer first, then by feature/group.

Example:

```txt
adapter
  ledger-service
    dto
      account
      account-classificaiton
      account-types
      journal-entry
```

## Layer Responsibilities

### Presentation

- Angular components
- HTML templates
- SCSS/CSS styles
- user interaction
- display state only

Rules:
- No business logic
- No direct HTTP calls
- Keep components small and focused

### Application

- Contains use cases
- Orchestrates domain + infrastructure calls
- Everything is handled using dependency inversion

### Domain

- Core business logic
- There should be little to none of this in this project unless 100% Needed. Perfer deligating this to the micro-services and keep the UI as "dummy UI" as possible

### adapter

- HTTP/API calls
- DTO mapping
- external integrations
- repository implementations


# UI Design

## Component Rules

- Keep components focused on one purpose
- Prefer composition over large all-in-one components
- Reusable display pieces should be extracted into smaller components when it improves clarity
- Do not create extra components for trivial markup

## Page Rules

- Pages should coordinate filters, calls to the application layer, and display state
- Pages should not contain business rules
- Pages should pass display data down into smaller components

## Forms

- Forms should be easy to read and easy to follow
- Prefer explicit field names over overly generic models
- Validation messaging should be clear and predictable

---

# Data Handling

- Avoid trusting client-side input
- Use DTOs for all external communication
- Do not let raw API shapes leak throughout the UI
- Map external DTOs into project models before broad use, even when thos project model are identical. I should not need to go look
in a micro service to figure out the shape of data coming in from it.

---

# Code Cleanliness

- Remove unused imports
- Remove commented-out code
- Keep methods short and focused
- Avoid deep nesting
- Avoid overly clever chains when a simpler approach is more readable

## Dependency Injection
- Perfer constructor based DI over inject()

## Typing
-Always put a type on new varibles even when typescript would not require it. If we do not know the type mark this by typing it as an any object. If we know that it can be more then one type (some librarys designed for javascript do this) then make it a composite type. Examples of both below.

```typescript
varibleNormal: String,
varibleUnknownType: any,
varibleCompositeType: (String | Number),
```


# Anti-Patterns

Avoid:

- Massive components
- Business logic in components
- Direct HTTP calls in components
- Generic utility files like `utils.ts`
- Overuse of inheritance
- Use of global state


# Testing Philosophy

- Focus on:
  - application logic
  - important component behavior

- Avoid over-testing:
  - trivial mappings
  - simple getters/setters
  - markup with no meaningful behavior

# Error Handling

- Handle API failures in a predictable way
- Do not silently swallow errors
- Surface meaningful error states in the UI
- Keep user-facing error messages clear and simple

# Something not covered?

Choose the option that is easiest to understand and maintain later.
