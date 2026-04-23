# User service guide

**Purpose**: Provide a navigable overview of `user-service` documentation.
<br>
Last updated: 2026-04-22

## Overview

&nbsp;&nbsp;&nbsp;&nbsp; The user-service is responsible for authentication, authorization, and identity management across the system. It acts as the central authority for issuing <br>
and validating JWTs, supporting both end-user authentication and service-to-service communication. The service generates tokens using RS256 and includes claims <br>
such as audience, scope, and token type to enable fine-grained access control. It also supports service authentication, allowing backend services to securely <br>
request scoped tokens when interacting with other services. By centralizing identity and token issuance, the user-service ensures consistent security policies <br>
across the entire system. This design enables a stateless and scalable authentication model suitable for distributed architectures.

## Responsibilities

- Register/authenticate users
- Issue/refresh access tokens and manage sessions
- Authenticate internal services for service-to-service calls

## Current capabilities

- `/user/register`, `/user/login`, `/user/logout`, `/user/refresh`
- `/user/service/login` for internal services (scoped audiences/scopes)
- Centralized exception handling and standard error envelope

## Tech stack

- Java 17
- Spring Boot (Web MVC, Data JPA, Flyway, Security)
- PostgreSQL
- Maven

## Docs

- [Architecture](./user-service/architecture.md)
- [Domain model](./user-service/domain-model.md)
- [Data design](./user-service/data-design.md)
- [API reference](./user-service/api-reference.md)
- [Error handling](./user-service/error-handling.md)
- [Logging & observability](./user-service/logging-observability.md)
- [Project structure](./user-service/project-structure.md)
- [Running locally](./user-service/running-locally.md)
- [Testing](./user-service/testing.md)
- [Use cases](./user-service/use-cases.md)

