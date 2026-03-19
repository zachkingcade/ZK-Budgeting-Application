# Ledger Service Logging

## Logging Strategy
- Logging API: SLF4J.
- Config backend: Logback via `logback-spring.xml`.
- Most logs are emitted in web controllers and application services.

## Levels
- Root logger: `INFO`.
- Package logger `zachkingcade.dev.ledger`: `DEBUG`.
- Spring: `INFO`.
- Hibernate: `WARN`.

## Appenders
- Console appender for all root logs.
- Rolling files by level:
  - `logs/ledger-service-info-current.log`
  - `logs/ledger-service-warn-current.log`
  - `logs/ledger-service-error-current.log`
- Daily rollovers with `maxHistory` of 30.

## Logging Patterns in Code
- Start/finish debug logs for most endpoint and service methods.
- Context fields like id, description, and line counts are included.
- Exceptions are logged with stack traces before rethrow.
