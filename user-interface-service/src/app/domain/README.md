Purpose: Explain what the UI domain layer contains (and does not contain).
Last updated: 2026-04-22

## Domain

If there are any domain/business rules about the user interface, they live here. Most business logic should be owned by backend microservices, with the UI acting as a thin client that maps API responses into UI models and renders state.