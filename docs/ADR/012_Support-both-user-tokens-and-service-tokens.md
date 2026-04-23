# ADR 0005: Support Both User Tokens and Service Tokens

## Context
The system includes both:
- Requests initiated by end users (via frontend clients)
- Requests initiated by backend services (e.g., reporting-service calling ledger-service)

A single token model does not sufficiently capture the differences in permissions and intent between these two use cases.

## Decision
The system will support two distinct types of JWTs:

### User Tokens
- Represent an authenticated end user
- `sub` contains the user ID
- Have full access to permitted operations within the system

### Service Tokens
- Represent a backend service
- `sub` contains the service name
- Include additional claims:
    - `token_type` (e.g., `user` or `service`)
    - `scope` (allowed operations)
    - `service_name`
    - `acting_for_user_id` (optional)

### Scope
Service-to-service communication will use JWTs with scoped permissions.

- Tokens include:
    - `scope`: Defines allowed operations (e.g., `ledger.accounts.read`)
- Receiving services:
    - Validate the token
    - Enforce scope-based access control

### Acting For
Service-to-service communication will use JWTs with the option to be acting in the interest of a certain user

- Tokens include:
    - `acting_for_user_id`: Identifies the user on whose behalf the service is acting (if applicable)
- Receiving services:
    - Validate the token
    - Resolve the effective user context from the token

### Life Cycle
Service tokens last for only 10 minutes and have no session to refresh. Often they will be minted for each user the service is acting in the stead of.


## Consequences

### Positive
- Clearly separates user-driven and system-driven actions.
- Enables fine-grained control over service permissions.
- Improves auditability by distinguishing who initiated an action.
- Supports secure service-to-service communication patterns.

### Negative
- Increases complexity in token validation logic.
- Requires consistent handling of multiple token types across services.
- Additional claims must be carefully validated.

## Alternatives Considered

### Single Token Type for All Requests
Use the same token structure for both users and services.

- **Pros**: Simpler implementation.
- **Cons**: Blurs responsibility boundaries and weakens security controls.