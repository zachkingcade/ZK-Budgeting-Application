# ADR 0014: Keep Angular Frontend Logic Minimal

## Context
The project includes an Angular frontend used to interact with backend microservices.

Because the backend is the primary focus of the project, the frontend is intended to:
- provide a clean and usable interface
- demonstrate integration with the backend
- avoid duplicating business rules already enforced by backend services

## Decision
The Angular frontend will keep application logic minimal.

The frontend will primarily be responsible for:
- rendering data
- collecting user input
- performing simple UI state management
- calling backend APIs

Core business rules, validation of domain behavior, and authoritative calculations will remain in backend services.

## Consequences

### Positive
- Keeps business logic centralized in the backend.
- Reduces duplication of rules across frontend and backend.
- Makes the frontend easier to maintain.
- Reinforces the project’s emphasis on backend architecture and domain design.
- Is a fairly common industry practice