--Locking down unused default public schema
REVOKE ALL ON SCHEMA public FROM PUBLIC;

--Setup role
CREATE ROLE ledger_svc LOGIN PASSWORD 'ledger_svc';

--Create new database schema
CREATE SCHEMA IF NOT EXISTS ledger AUTHORIZATION ledger_svc;

-- Permissions
GRANT USAGE, CREATE ON SCHEMA ledger TO ledger_svc;
ALTER ROLE ledger_svc SET search_path='ledger';