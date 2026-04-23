Purpose: Describe how `user-service` represents and propagates errors.
Last updated: 2026-04-22

## Error response format

Backend services in this repo follow a consistent error shape:

```json
{
  "errorCode": "DOMAIN_ERROR",
  "message": "Human readable message"
}
```

## Mappings

Source: `GlobalExceptionHandler`

- `DomainException` → `400 BAD_REQUEST`, `DOMAIN_ERROR`
- `IllegalArgumentException` → `400 BAD_REQUEST`, `BAD_REQUEST`
- `ApplicationException` → `400 BAD_REQUEST`, `APPLICATION_ERROR`
- `NotFoundException` → `404 NOT_FOUND`, `NOT_FOUND`
- `MethodArgumentNotValidException` → `400 BAD_REQUEST`, `BAD_REQUEST` (message is first field error)
- fallback `Exception` → `500 INTERNAL_SERVER_ERROR`, `INTERNAL_ERROR`

## Canonical source

Canonical mapping lives in:

- [`../../user-service/src/main/java/zachkingcade/dev/user/adapter/web/dto/GlobalExceptionHandler.java`](../../user-service/src/main/java/zachkingcade/dev/user/adapter/web/dto/GlobalExceptionHandler.java)

