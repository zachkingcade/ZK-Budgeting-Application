# ADR 0016: Avoid Storing Derived Data

## Context
The system manages financial data using double-entry accounting principles. Account balances can be derived from journal entries and their associated lines.

A decision was required on whether to:
- Store calculated values such as account balances directly in the database
- Or compute them dynamically from underlying transaction data

Storing derived data introduces the risk of inconsistencies if the stored values become out of sync with the source data.

## Decision
Derived data such as account balances will not be stored directly.

- Balances will be calculated dynamically from journal entries and lines.
- The system will treat journal data as the single source of truth.
- Any reporting or aggregation will compute values based on this underlying data.

## Consequences

### Positive
- Ensures data integrity by avoiding drift between stored and computed values.
- Keeps the system aligned with accounting principles where transactions are authoritative.
- Eliminates the need for reconciliation logic between stored and derived values.

### Negative
- May increase computational cost for balance-related queries.
- Requires more complex queries or aggregation logic.
- Can impact performance for large datasets if not optimized.

## Alternatives Considered

### Store Running Balances
Maintain a running balance column on accounts.

- **Pros**: Faster read performance for balance queries.
- **Cons**: Risk of data drift, requires careful synchronization and reconciliation.