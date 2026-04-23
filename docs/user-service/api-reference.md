# User service API reference

**Purpose**: Explain how to talk to `user-service` over HTTP and what conventions it follows.
<br>
Last updated: 2026-04-22

## Canonical sources

This page explains the API at a human level. For cross-service envelopes and conventions, refer to:

- Service guide: [`../guide-user-service.md`](../guide-user-service.md)
- Microservice conventions: [`../standards/microservice-springboot-consistency.md`](../standards/microservice-springboot-consistency.md)

## Base URL

- `http://localhost:8082`

## Envelope conventions

All successful JSON responses return an `ApiResponse<T>` envelope:

```json
{
  "statusMessage": "Human readable status",
  "metaData": {
    "requestDate": "YYYY-MM-DD",
    "requestTime": "HH:mm:ss.SSS",
    "executionTimeMs": 12,
    "dataResponseCount": 1
  },
  "data": {}
}
```

Errors return `ApiErrorResponse`:

```json
{
  "errorCode": "BAD_REQUEST",
  "message": "Human readable message"
}
```

## Endpoints

All routes are under the `/user` prefix.

### User authentication

| Method | Path | Summary | Request DTO | Response DTO | Status |
|---|---|---|---|---|---|
| POST | `/user/register` | Register a user | [`RegisterUserRequest`](../../user-service/src/main/java/zachkingcade/dev/user/adapter/web/dto/user/RegisterUserRequest.java) | [`RegisterUserResponse`](../../user-service/src/main/java/zachkingcade/dev/user/adapter/web/dto/user/RegisterUserResponse.java) | 201 |
| POST | `/user/login` | Login a user | [`LoginUserRequest`](../../user-service/src/main/java/zachkingcade/dev/user/adapter/web/dto/user/LoginUserRequest.java) | [`LoginUserResponse`](../../user-service/src/main/java/zachkingcade/dev/user/adapter/web/dto/user/LoginUserResponse.java) | 202 |
| POST | `/user/logout` | Logout a user | [`LogoutUserRequest`](../../user-service/src/main/java/zachkingcade/dev/user/adapter/web/dto/user/LogoutUserRequest.java) | `String` | 200 |
| POST | `/user/refresh` | Refresh access token | [`RefreshLoginRequest`](../../user-service/src/main/java/zachkingcade/dev/user/adapter/web/dto/user/RefreshLoginRequest.java) | [`RefreshLoginResponse`](../../user-service/src/main/java/zachkingcade/dev/user/adapter/web/dto/user/RefreshLoginResponse.java) | 200 |

### Service authentication

| Method | Path | Summary | Request DTO | Response DTO | Status |
|---|---|---|---|---|---|
| POST | `/user/service/login` | Service login and token minting | [`ServiceLoginRequest`](../../user-service/src/main/java/zachkingcade/dev/user/adapter/web/dto/user/ServiceLoginRequest.java) | [`ServiceLoginResponse`](../../user-service/src/main/java/zachkingcade/dev/user/adapter/web/dto/user/ServiceLoginResponse.java) | 200 |

