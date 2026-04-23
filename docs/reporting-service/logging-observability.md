Purpose: Explain how to observe `reporting-service` behavior (logs, levels, debugging workflow).
Last updated: 2026-04-22

## Logging stack

- SLF4J + Logback (`reporting-service/src/main/resources/logback-spring.xml`)

## Notes

- Prefer logging identifiers/counts instead of payloads.
- Avoid logging secrets (service secrets, tokens).

