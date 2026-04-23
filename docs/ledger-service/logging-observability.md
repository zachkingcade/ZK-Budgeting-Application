Purpose: Explain how to observe `ledger-service` behavior (logs, levels, and debugging workflow).
Last updated: 2026-04-22

## Logging stack

- API: SLF4J
- Backend: Logback (`ledger-service/src/main/resources/logback-spring.xml`)

## Levels

- Root logger: `INFO`
- Service package (`zachkingcade.dev.ledger`): `DEBUG`
- Spring: `INFO`
- Hibernate: `WARN`

## Appenders and files

- Console logging for all root logs
- Rolling files by level under `logs/` (ledger service name in filename)

## Correlation and troubleshooting

- Prefer logging **identifiers and counts** (IDs, sizes), not full payloads.
- Avoid logging secrets/credentials.
- Log failures with stack traces at `error`.

