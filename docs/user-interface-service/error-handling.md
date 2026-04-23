Purpose: Explain how the UI represents and surfaces errors to users.
Last updated: 2026-04-22

## Error handling goals

- Surface actionable errors to users without exposing internals.
- Keep error states predictable at the page/component level.

## Canonical source

This page describes intent; canonical behavior lives in code:

- Auth guard and auth pages: [`../../user-interface-service/src/app/presentation/auth/`](../../user-interface-service/src/app/presentation/auth/)
- UI routing: [`../../user-interface-service/src/app/app.routes.ts`](../../user-interface-service/src/app/app.routes.ts)

