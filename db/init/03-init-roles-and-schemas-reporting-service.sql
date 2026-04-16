--Setup role
CREATE ROLE reporting_owner LOGIN PASSWORD 'reporting_owner';

--Create new database schema
CREATE SCHEMA IF NOT EXISTS reporting AUTHORIZATION reporting_owner;

-- Permissions
GRANT USAGE, CREATE ON SCHEMA reporting TO reporting_owner;
ALTER ROLE reporting_owner SET search_path='reporting';