# Reporting service testing

**Purpose**: Explain how `reporting-service` is tested and how to run the test suite.
<br>
Last updated: 2026-04-22

Back to: [Reporting service guide](../guide-reporting-service.md)

## Running tests

From `reporting-service/`:

```bash
./mvnw test
```

## Testing philosophy

Our default testing approach is:

- **1 happy path** per use case
- **All known sad paths** (invalid report type, unauthorized access, not found, etc.)

## What we intentionally do not test

- Framework behavior (Spring/Jackson internals)
- Logging output content/format
- Mechanical DTO mappings

