# User interface service project structure

**Purpose**: Explain where code lives in `user-interface-service` so contributors can navigate quickly.
<br>
Last updated: 2026-04-22

Back to: [User interface service guide](../guide-user-interface-service.md)

## High-level layout

- `src/app/presentation`: components/pages and UI state
- `src/app/application`: application services / orchestration
- `src/app/domain`: UI domain models (minimize business logic here; prefer backend ownership)
- `src/app/adapter`: API clients and DTO mapping

Key entrypoints:

- Routes: [`../../user-interface-service/src/app/app.routes.ts`](../../user-interface-service/src/app/app.routes.ts)
- Presentation: [`../../user-interface-service/src/app/presentation/`](../../user-interface-service/src/app/presentation/)
- Adapters: [`../../user-interface-service/src/app/adapter/`](../../user-interface-service/src/app/adapter/)

## Reference

- Angular standards: [`../standards-angular.md`](../standards/standards-angular.md)

