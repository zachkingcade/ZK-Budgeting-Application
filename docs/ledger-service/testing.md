# Ledger service testing

**Purpose**: Explain how `ledger-service` is tested and how to run the test suite.
<br>
Last updated: 2026-04-22

Back to: [Ledger service guide](../guide-ledger-service.md)

## Running tests

From `ledger-service/`:

```bash
./mvnw test
```

## What we test

Our default testing approach is:

- **1 happy path** per use case
- **All known sad paths** (domain validation failures, bad inputs, known policy violations)

This keeps coverage high on correctness without bloating the test suite.

## What we intentionally do not test

- Framework behavior (Spring/JPA internals)
- Logging output content/format
- Trivial DTO getters/setters and mechanical mappings


