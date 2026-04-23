Purpose: Provide a high-level introduction and pointers to canonical documentation.
Last updated: 2026-04-22

# ZK-Budgeting-Application

&nbsp;&nbsp;&nbsp;&nbsp; This project is a full-stack financial tracking system designed to demonstrate modern backend engineering practices using a microservices architecture. <br>
It centers around accurate double-entry accounting, with a strong emphasis on data integrity, clear service boundaries, and maintainable system design. <br>
The system is composed of independent services responsible for authentication, transactional data, and reporting, each with well-defined responsibilities. <br>
Communication between services is secured using scoped JWTs, ensuring both flexibility and safety in a distributed environment. Reporting is handled <br>
asynchronously to support more complex workloads without impacting core operations. Overall, the project prioritizes correctness, scalability, and <br>
clarity over shortcuts, making it a strong representation of real-world system design.

## Start here

- **Documentation hub**: [`./docs/README.md`](./docs/README.md)
- **Service guides**:
  - [`./docs/ledger-service.md`](docs/guide-ledger-service.md)
  - [`./docs/user-service.md`](docs/guide-user-service.md)
  - [`./docs/reporting-service.md`](docs/guide-reporting-service.md)
  - [`./docs/user-interface-service.md`](docs/guide-user-interface-service.md)

## Running locally (quick)

From repository root:

```bash
docker compose up -d
```

UI: `http://localhost:4200`

## Backup Postgres database (local dev)

```bash
docker exec -t ledger-postgres pg_dump -U postgres -d app > local_db_backup/YYMMDD_backup.sql
```