# ADR 0001: Use Clean Architecture per Microservice

## Context
A key goal of this project is to demonstrate strong separation of concerns and maintainability, particularly for portfolio and interview purposes. <br>
I want to demonstrate skills that would actually be used in industry as well as demonstrate me abilities as an architect and engineer.

## Decision
Each microservice will follow a Clean Architecture structure with clearly defined layers:

- **Domain Layer**: Contains core business logic, entities, and rules. This layer has no dependencies on lower layers.
- **Application Layer**: Handles use cases and coordinates interactions between domain and external layers.
- **Adapter Layers**:
  - **Web Adapter**: Handles HTTP requests and responses.
  - **Persistence Adapter**: Handles database interactions.

Dependencies flow inward, with outer layers depending on inner layers, but not vice versa.

## Consequences

### Positive
- Strong separation of concerns improves maintainability.
- Business logic is isolated and easier to test.
- Frameworks (Spring, JPA, etc.) can be replaced with minimal impact on core logic.
- Aligns with widely recognized architectural best practices.

### Negative
- Increased initial complexity compared to simpler layered architectures.
- Requires discipline to maintain boundaries between layers.
- More boilerplate code, especially for mapping between layers.

## Alternatives Considered

### Traditional Layered Architecture
A simpler approach where controllers, services, and repositories are tightly coupled.

- **Pros**: Faster to implement, less boilerplate.
- **Cons**: Business logic becomes dependent on frameworks, harder to test and evolve.

### Monolithic Domain Model with Direct Persistence Access
Allowing domain objects to directly interact with persistence frameworks.

- **Pros**: Simpler implementation.
- **Cons**: Violates separation of concerns and tightly couples domain logic to infrastructure.