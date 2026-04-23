# ADR 0004: Use JWT-Based Authentication with RS256

## Context
The system consists of multiple services that require authentication and authorization. The authentication mechanism must support:

- Stateless verification across services
- Secure token validation
- Scalability without centralized session storage

## Decision
Authentication will be implemented using JSON Web Tokens (JWTs) signed with the RS256 (RSA) algorithm.

- Tokens are issued by the **user-service** (acting as the authentication authority).
- Tokens include standard claims such as:
    - `sub` (subject / user ID)
    - `iss` (issuer)
    - `aud` (audience)
    - `exp` (expiration)
- Services validate tokens using a public key and enforce:
    - Correct issuer
    - Matching audience
    - Valid signature and expiration
- When logging in a user will be given a token and a session
- Sessions will be active for 24 hours and will allow retrieving new token within that time
- Tokens will exist for 15 minutes

## Consequences

### Positive
- Stateless authentication allows services to validate tokens without database lookups.
- RS256 enables secure verification using public/private key pairs.
- Scales well across multiple services.
- Aligns with common industry practices for distributed systems.

### Negative
- Requires proper key management and rotation strategy.
- Tokens cannot be easily revoked before expiration without additional mechanisms.
- Adds complexity compared to session-based authentication.

## Alternatives Considered

### Session-Based Authentication
Server maintains session state for each user.

- **Pros**: Simple, easy to revoke sessions.
- **Cons**: Not scalable across multiple services without shared session storage.

### Symmetric JWT
Tokens signed and verified using a shared secret.

- **Pros**: Simpler to implement.
- **Cons**: All services must share the same secret, increasing security risk and coupling.