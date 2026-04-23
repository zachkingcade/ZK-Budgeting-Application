Purpose: Explain how to debug `user-interface-service` issues locally and in production-like environments.
Last updated: 2026-04-22

## Observability

- Use browser devtools (network tab, console) to diagnose API failures.
- Prefer logging user-safe identifiers and counts (not sensitive payloads).

## Canonical source

This page describes the workflow; canonical integrations (if any) live in code:

- UI entrypoints: [`../../user-interface-service/src/app/`](../../user-interface-service/src/app/)

