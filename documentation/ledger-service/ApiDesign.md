# Ledger Service API Design

## Base Characteristics
- Style: JSON over HTTP with Spring MVC controllers.
- URI pattern: resource-based with explicit operation segments (`/all`, `/byid/{id}`, `/add`, `/update`).
- DTO style: Java `record` request/response types under `adapter/in/web/dto`.

## Endpoints
### Accounts (`/accounts`)
- `GET /all`
- `GET /byid/{id}`
- `POST /add`
- `POST /update`

### Account Types (`/accounttypes`)
- `GET /all`
- `GET /byid/{id}`
- `POST /add`
- `POST /update`

### Account Classifications (`/accountclassifications`)
- `GET /all`
- `GET /byid/{id}`

### Journal Entries (`/journalentry`)
- `GET /all`
- `GET /byid/{id}`
- `POST /add`
- `POST /update`

## Status Codes
- Most reads and updates return `200 OK`.
- `POST /accounts/add` returns `201 CREATED`.

## Validation Strategy
- Input and business validation occurs in the domain layer and application services.

## DTO Organization
- `dto/account/*`
- `dto/accounttype/*`
- `dto/accountclassifcation/*`
- `dto/journal/*`

## Known Consistency Opportunities
- Consider endpoint naming modernization (for example RESTful plural resources with standard methods).
