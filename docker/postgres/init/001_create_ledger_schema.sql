-- Schema for the Ledger service
CREATE SCHEMA IF NOT EXISTS ledger;

-- Create a dedicated DB user for the Ledger service
DO $$
BEGIN
  IF NOT EXISTS (SELECT FROM pg_roles WHERE rolname = 'ledger_svc') THEN
CREATE ROLE ledger_svc LOGIN PASSWORD 'ledger_svc';
END IF;
END$$;

-- Give the service access ONLY to its schema
GRANT USAGE ON SCHEMA ledger TO ledger_svc;
GRANT CREATE ON SCHEMA ledger TO ledger_svc;

-- Make sure future tables/sequences in ledger are usable by ledger_svc
ALTER DEFAULT PRIVILEGES IN SCHEMA ledger
  GRANT SELECT, INSERT, UPDATE, DELETE ON TABLES TO ledger_svc;

ALTER DEFAULT PRIVILEGES IN SCHEMA ledger
  GRANT USAGE, SELECT, UPDATE ON SEQUENCES TO ledger_svc;

-- Set search_path for that user
ALTER ROLE ledger_svc SET search_path = ledger;