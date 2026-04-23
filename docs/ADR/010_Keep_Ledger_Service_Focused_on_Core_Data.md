# ADR 0007: Keep Ledger Service Focused on Core Data

## Context
The ledger-service is responsible for storing and managing core financial data such as accounts, journal entries, and transaction lines.

There is a need to support reporting features such as:
- Account balances as of a specific date
- Aggregated financial summaries
- Historical reporting

A decision was required on whether to implement these calculations within the ledger-service or separate them into another component.

## Decision
The ledger-service will remain focused solely on core data storage and validation.

- It will expose raw financial data through read APIs.
- It will **not** include reporting-specific logic such as:
    - “balance as of date”
    - aggregated summaries
    - derived reporting calculations

All reporting and aggregation logic will be handled a new reporting-service.

## Consequences

### Positive
- Clear separation of responsibilities between services.
- Ledger remains simple, predictable, and focused on correctness.
- Reporting logic can evolve independently without impacting core transaction logic.
- Avoids coupling ledger to specific reporting requirements.

### Negative
- Reporting service must perform additional data processing.
- Some queries may require transferring larger datasets.
- Increased complexity due to cross-service communication.

## Alternatives Considered

### Include Reporting Logic in Ledger Service
Implement balance calculations and reporting endpoints directly in the ledger.

- **Pros**: Simpler architecture, fewer services involved.
- **Cons**: Tight coupling between transactional data and reporting needs, harder to maintain and extend.