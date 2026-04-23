Purpose: Describe how `ledger-service` represents and propagates errors.
Last updated: 2026-04-22

## Error response format

All error responses use a consistent shape:

```json
{
  "errorCode": "DOMAIN_ERROR",
  "message": "Human readable message"
}
```

## Exception flow

- A centralized exception handler translates exceptions into HTTP responses.
- Controllers/services may log and rethrow; the handler is responsible for the final response mapping.

## Exception mappings

- `DomainException` → `400 BAD_REQUEST`, `DOMAIN_ERROR`
- `IllegalArgumentException` → `400 BAD_REQUEST`, `BAD_REQUEST`
- `ApplicationException` → `500 INTERNAL_SERVER_ERROR`, `APPLICATION_ERROR`
- Any other `Exception` → `500 INTERNAL_SERVER_ERROR`, `INTERNAL_ERROR`

