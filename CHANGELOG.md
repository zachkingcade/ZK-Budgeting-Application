# Changelog

Purpose: Track notable changes to the project across released versions. <br>
Last updated: 2026-04-22

## Version 1.0.0

Minimal viable product (MVP). Development history spans 2026-02-26 → Ongoing.

### Added

- Repository + backend project initialized
- Local Postgres via `docker-compose.yml`
- Ledger service domain model, repositories, and initial HTTP endpoints
- Ledger features: accounts, account types, account classifications, journal entries
- Logging and request metadata enrichment for API responses
- Sorting and filtering for account/account-type/journal-entry searches
- Enriched data returned on account and journal-entry calls
- Angular UI initialized, including the initial ledger page and journal entry work
- Accounts/account-types management pages
- User authentication: register/login endpoints + UI
- System-owned/protected account types and user-based data ownership enforcement
- Service-to-service token minting in `user-service`
- Service token auth in `ledger-service`
- `reporting-service` microservice with two starting reports
- Reports page in the UI
- Pending transactions page in the UI
- Consolidated Dockerfiles across services and a unified `docker compose` setup
- Initial continuous integration GitHub Actions workflow
- Initial image publishing GitHub Actions workflow

