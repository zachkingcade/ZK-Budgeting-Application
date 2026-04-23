# User service domain model

**Purpose**: Define the core business concepts owned by `user-service`.
<br>
Last updated: 2026-04-22

Back to: [User service guide](../guide-user-service.md)

## Domain concepts

`user-service` owns identity concepts such as users, credentials, sessions, and service permissions used to mint access tokens for users and internal services.

## Concepts

- **User**: identity with a username and password hash.
- **UserSession**: persisted session token with creation/expiration timestamps.
- **ServicePermission**: permission record for internal services (service name + secret hash + allowed audiences/scopes + whether it may act for a user).

## Relationships

- A `UserSession` belongs to exactly one `User`.

## Policies and invariants

- Usernames are unique.
- Session tokens are unique and must be associated with a valid user id.
- Service names are unique and must authenticate using a BCrypt-hashed secret.

## Canonical source

This page describes intent; canonical definitions live in code:

- Domain: [`../../user-service/src/main/java/zachkingcade/dev/user/domain/`](../../user-service/src/main/java/zachkingcade/dev/user/domain/)
- Persistence entities: [`../../user-service/src/main/java/zachkingcade/dev/user/adapter/persistence/jpa/`](../../user-service/src/main/java/zachkingcade/dev/user/adapter/persistence/jpa/)

