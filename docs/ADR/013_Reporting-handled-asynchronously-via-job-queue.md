# ADR 0008: Handle Reporting Asynchronously via Job Queue

## Context
The reporting-service is responsible for generating reports as PDFs.

These operations:
- Can be computationally expensive
- May involve large datasets
- May take longer than acceptable HTTP request timeouts

A decision was required on whether report generation should be synchronous (request/response) or asynchronous.

## Decision
Report generation will be handled asynchronously using a job queue model.

- When a report is requested:
    - A job is created and stored in the database (`report_jobs`)
    - Status is set to `QUEUED`
- A background process:
    - Polls for jobs
    - Processes them
    - Updates status (`IN_PROGRESS`, `COMPLETED`, `FAILED`)
- Completed reports are stored (e.g., as PDF in `completed_reports`)
- Clients retrieve reports after completion

## Consequences

### Positive
- Prevents long-running HTTP requests and timeouts.
- Improves scalability for large or complex reports.
- Allows retry and failure handling.
- Provides visibility into report status.

### Negative
- Increased complexity compared to synchronous processing.
- Requires job tracking and lifecycle management.
- Users must handle delayed responses (polling or refresh).

## Alternatives Considered

### Synchronous Report Generation
Generate reports during the HTTP request.

- **Pros**: Simpler implementation, immediate response.
- **Cons**: Risk of timeouts, poor performance for large datasets.

### External Queue System
Use a dedicated message broker.

- **Pros**: More scalable and robust queueing.
- **Cons**: Additional infrastructure complexity not necessary for current scope.