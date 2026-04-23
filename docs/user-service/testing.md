# User service testing

**Purpose**: Explain how `user-service` is tested and how to run the test suite.
<br>
Last updated: 2026-04-22

Back to: [User service guide](../guide-user-service.md)

## Running tests

From `user-service/`:

```bash
./mvnw test
```

## What we test

Our default testing approach is:

- **1 happy path** per use case
- **All known sad paths** (invalid credentials, expired sessions, permission violations, etc.)

## What we intentionally do not test

- Framework behavior (Spring Security internals)
- Logging output content/format
- Trivial DTO getters/setters and mechanical mappings

