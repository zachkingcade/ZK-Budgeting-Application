# ADR 0015: Use Docker Compose for Local Development

## Context
The project consists of multiple services and supporting infrastructure such as Postgres. Running all components locally requires a repeatable and consistent development setup.

## Decision
Docker Compose will be used to run the local development environment.

Docker Compose will be responsible for orchestrating:
- application services
- supporting infrastructure such as Postgres
- network configuration between containers
- shared startup patterns for local development

This provides a single, consistent mechanism for bringing the local environment up and down.

## Consequences

### Positive
- Simplifies local setup and onboarding.
- Makes development environments more consistent across machines.
- Reduces manual configuration drift.
- Works well for multi-service projects with shared infrastructure needs.
- Prevents the "it works on my computer" issue with working on multiple systems or OS.

### Negative
- Adds Docker-related complexity for developers unfamiliar with containers.