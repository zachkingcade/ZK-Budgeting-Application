Purpose: Describe how `reporting-service` represents and propagates errors.
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

- `IllegalArgumentException` → `400 BAD_REQUEST`, `BAD_REQUEST`
- `AccessDeniedException` → `403 FORBIDDEN`, `FORBIDDEN`
- `ResponseStatusException` → status from exception, `errorCode` set to the status string (e.g. `404 NOT_FOUND`)
- fallback `Exception` → `500 INTERNAL_SERVER_ERROR`, `INTERNAL_ERROR`

## Canonical source

Canonical mapping lives in:

- [`../../reporting-service/src/main/java/zachkingcade/dev/reporting/adapter/web/dto/GlobalExceptionHandler.java`](../../reporting-service/src/main/java/zachkingcade/dev/reporting/adapter/web/dto/GlobalExceptionHandler.java)

