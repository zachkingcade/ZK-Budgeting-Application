# ADR 0011: Use a Standard API Response Envelope

## Context
The system exposes multiple endpoints across services. Without a consistent response structure, clients must handle each endpoint differently, increasing frontend complexity and making debugging more difficult.

A decision was required on whether responses should be returned as raw payloads or wrapped in a standard envelope.

## Decision
API responses will use a standard response envelope.

Successful responses will use a consistent wrapper such as `ApiResponse`, and error responses will use a consistent wrapper such as `ApiErrorResponse`.

The response envelope provides a predictable structure for:
- returned data
- status or success indicators
- human-readable messages where appropriate
- error details in failure scenarios

## Consequences

### Positive
- Creates consistency across endpoints and services.
- Simplifies frontend handling and parsing.
- Makes error and success responses easier to understand and debug.
- Supports a more professional and predictable API contract.

### Negative
- Adds slight verbosity compared to returning raw payloads.
- Can feel unnecessary for very small or simple endpoints.
- Requires discipline to keep the envelope consistent across services.

## Alternatives Considered

### Raw Payload Responses
Return domain objects or DTOs directly without a wrapper.

- **Pros**: Simpler response shape, less boilerplate.
- **Cons**: Inconsistent handling of metadata and errors, harder for clients to standardize behavior.

### Per-Endpoint Custom Response Shapes
Allow each endpoint to define its own response structure.

- **Pros**: Maximum flexibility.
- **Cons**: Leads to inconsistent API design and increases client complexity.