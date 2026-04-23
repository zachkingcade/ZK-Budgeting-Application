# User interface service testing

**Purpose**: Explain how `user-interface-service` is tested and how to run the test suite.
<br>
Last updated: 2026-04-22

Back to: [User interface service guide](../guide-user-interface-service.md)

## Running tests

From `user-interface-service/`:

```bash
npm test
```

## Testing philosophy

Our default testing approach is:

- **1 happy path** per component/page behavior
- **All known sad paths** (API failures, empty states, invalid form input, etc.)

## What we intentionally do not test

- Third-party library internals (Angular, Material)
- Cosmetic styling details

