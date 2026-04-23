Purpose: Describe key workflows supported by `user-service`.
Last updated: 2026-04-22

## Use cases

- **Register user**: create a new `users` row and store a password hash.
- **Login user**: validate credentials, create a `user_sessions` row, return session token + access token material.
- **Logout user**: invalidate a session token (session is no longer accepted).
- **Refresh session**: validate session token and expiration; return a new access token if still valid.
- **Service login**: authenticate a microservice via `service_permissions`, then mint an access token constrained to allowed audiences/scopes and (optionally) an acting user id.

## Canonical source

This page describes intent; canonical entrypoints live in code:

- Controller routes: [`../../user-service/src/main/java/zachkingcade/dev/user/adapter/web/UserController.java`](../../user-service/src/main/java/zachkingcade/dev/user/adapter/web/UserController.java)
- DTOs: [`../../user-service/src/main/java/zachkingcade/dev/user/adapter/web/dto/user/`](../../user-service/src/main/java/zachkingcade/dev/user/adapter/web/dto/user/)

