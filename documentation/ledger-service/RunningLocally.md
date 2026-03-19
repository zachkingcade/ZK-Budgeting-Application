# Running Ledger Service Locally

## Prerequisites
- Java 17
- Docker and Docker Compose

## 1) Start PostgreSQL
From repository root:

```bash
docker compose up -d postgres
```
This starts PostgreSQL (`budgeting-postgres`) on `localhost:5432` and runs initialization scripts from `docker/postgres/init`. <br>
*Note: if you're using Intellij there is a saved run profile in the repo* 

## 2) Run the Service
From `ledger-service`:

```bash
./mvnw spring-boot:run
```
*Note: if you're using Intellij there is a saved run profile in the repo*

Default service port: `8081`.

## 3) Verify Basic Startup
- Check service logs in console.
- Confirm endpoint example:

```bash
curl http://localhost:8081/accounts/all
```

## Tests
From `ledger-service`:

```bash
./mvnw test
```