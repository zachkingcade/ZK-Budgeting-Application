# ADR 0013: Use a Request Timing and Logging Filter

## Context
The system exposes HTTP APIs across multiple services. During development and troubleshooting, it is important to understand:

- when requests enter and leave the system
- how long requests take to process
- which endpoints are being called

Without a centralized request logging strategy, visibility into runtime behavior is limited and debugging becomes slower and more inconsistent.

## Decision
Each service will include a request timing and logging filter that runs for incoming HTTP requests.

The filter will:
- log the incoming request path and method
- measure total request duration
- log completion status and timing
- provide a consistent place to add request-level observability behavior

This filter is intended to support operational visibility without only placing logging concerns directly in controllers.

## Consequences

### Positive
- Improves visibility into API behavior during development and troubleshooting.
- Makes it easier to identify slow endpoints.
- Keeps request-level logging logic centralized and consistent.
- Supports future observability improvements such as correlation IDs or structured logging.