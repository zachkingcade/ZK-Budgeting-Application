Purpose: Explain how to observe `user-service` behavior (logs, levels, debugging workflow).
Last updated: 2026-04-22

## Logging stack

- SLF4J + Logback (`user-service/src/main/resources/logback-spring.xml`)

## Log files

Logs roll under `logs/` by level, with filenames prefixed by `user-service-`.

## Notes

- Prefer logging identifiers/counts instead of payloads.
- Avoid logging secrets (credentials, private keys).

