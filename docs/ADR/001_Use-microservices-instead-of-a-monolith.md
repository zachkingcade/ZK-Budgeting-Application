# ADR 0003: Use Microservices Instead of a Monolith

## Context
The system is being designed as a portfolio project intended to demonstrate backend engineering skills, including system design, service boundaries, and scalability considerations.

A key decision is whether to implement the system as:
- A single monolithic application
- Multiple independently deployed microservices

## Decision
The system will be implemented as a set of microservices, with each service responsible for a specific domain:

- **user-service**: Authentication and user management
- **ledger-service**: Core financial data and transactions
- **reporting-service**: Aggregation and report generation

Services communicate over HTTP and are independently deployable.

## Consequences

### Positive
- Clear separation of domain responsibilities.
- Enables independent development and scaling of services.
- Demonstrates real-world system design patterns relevant for interviews.
- Encourages well-defined APIs and service contracts.

### Negative
- Increased complexity compared to a monolith.
- Requires handling of inter-service communication and authentication.
- More overhead in local development and deployment.
- Requires additional patterns (e.g., service authentication, API contracts).

## Alternatives Considered

### Monolithic Architecture
All functionality exists within a single application.

- **Pros**: Simpler development, easier debugging, fewer moving parts.
- **Cons**: Harder to scale, weaker separation of concerns, less representative of distributed systems.

### Modular Monolith
Single deployable unit with internal modular boundaries.

- **Pros**: Simpler deployment while maintaining some separation.
- **Cons**: Does not fully demonstrate distributed system concerns such as service communication and independent scaling.