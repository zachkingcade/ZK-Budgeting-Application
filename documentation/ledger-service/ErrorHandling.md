# Ledger Service Error Handling

## Exception Flow
- Controllers and services log and rethrow runtime exceptions.
- `GlobalExceptionHandler` centralizes translation to API responses.
- API errors are returned as `ApiErrorResponse(errorCode, message)`.

## Exception Mappings
- `DomainException` -> `400 BAD_REQUEST`, `DOMAIN_ERROR`
- `IllegalArgumentException` -> `400 BAD_REQUEST`, `BAD_REQUEST`
- `ApplicationException` -> `500 INTERNAL_SERVER_ERROR`, `APPLICATION_ERROR`
- generic `Exception` -> `500 INTERNAL_SERVER_ERROR`, `INTERNAL_ERROR`

## Practical Meaning
- Domain issues (invalid accounting state, invalid model values) are treated as client errors.
- Unexpected errors and application-policy failures are treated as server errors.
