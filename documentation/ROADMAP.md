## Roadmap to Interview Minimal Viable Product (IMVP) to show

## Next 10 steps
7. Implement Account Balance report
8. Implement Transaction Summary report
9. Add Docker + Docker Compose setup
10. Set up CI pipeline (build + test + lint)

## Features List
- Create account detail view (transaction history per account)
- Build Account Balance report
- Build Transaction Summary report (by date range + account type)
- Add form validation + user-friendly error messaging
- Add loading, empty, and error states

## Active Minor Fixes and Domain Rules to enforce later on
- Account should be at a balance of 0 before being allowed to close
- Searches should have an option to include "General Search" that includes items that have the included text in either description, notes or both.

## Stack List
- Implement Spring Security with JWT authentication
- Add user entity + ownership relationships in database
- Configure Flyway for database migrations
- Containerize services with Docker
- Create Docker Compose setup (app + database)
- Make GitHub repository look professional
- Implement CI/CD pipeline (GitHub Actions)
- Add backend unit testing (JUnit + Mockito)
- Add backend integration testing (API + DB)
- Add frontend linting (ESLint + Prettier)
- Add backend linting/style enforcement (Checkstyle or Spotless)
- Add code coverage reporting (JaCoCo)
- Implement environment-based configuration (profiles/env vars)
- Add API documentation (README + examples)
- Add basic architecture documentation

## Roadmap post IMVP

## Features List
- Implement accounting period close functionality
- Restrict or manage edits to closed periods
- Store/snapshot account balances at period close
- Build historical period reporting
- Implement revenue allocation / budgeting tool
- Allow defining allocation rules (percentages or categories)
- Generate planned allocations from revenue periods
- Expand reporting suite (budget vs actual, trends, etc.)
- Add CSV import/export for transactions
- Add audit log / change history tracking
- Implement role-based access control (optional multi-user roles)
- Add recurring journal entries
- Add notification/reminder system (optional)
- Add attachment/receipt support
- Improve mobile responsiveness


## Stack List
- Add Testcontainers for integration testing
- Add frontend testing suite (component + integration tests)
- Add Docker image builds in CI pipeline
- Add deployment pipeline (optional cloud hosting)
- Implement centralized logging strategy
- Add monitoring/health checks
- Add API versioning strategy
- Add secrets management improvements
- Add performance testing (basic load tests)
- Add caching layer if needed (Redis or similar)
- Add message broker if expanding services (Kafka/RabbitMQ)
- Expand CI pipeline (quality gates, coverage thresholds)
- Add error tracking/observability tooling