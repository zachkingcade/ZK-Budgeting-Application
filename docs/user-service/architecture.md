# User service architecture

**Purpose**: Describe how `user-service` is built and how it interacts with other services.
<br>
Last updated: 2026-04-22

Back to: [User service guide](../guide-user-service.md)

## Architecture

`user-service` is a Spring Boot microservice using a layered/ports-and-adapters style similar to other backend services in this repo.

## Clean Architecture in this service

- **Domain**: identity concepts and business rules (users, sessions, token policies).
- **Application**: use cases such as register/login/logout/refresh and service login.
- **Adapters**: HTTP controllers and DTOs, persistence adapters (JPA), security and configuration.

See: [`../standards/microservice-springboot-consistency.md`](../standards/microservice-springboot-consistency.md) and ADRs under `../ADR/`.

## Communication

- Other services call `user-service` for identity-related operations as configured by `USER_SERVICE_BASE_URL` in `docker-compose.yml`.
- Default local port: `8082`.

## Reference

- Cross-service patterns and envelopes: [`../microservice-springboot-consistency.md`](../standards/microservice-springboot-consistency.md)

