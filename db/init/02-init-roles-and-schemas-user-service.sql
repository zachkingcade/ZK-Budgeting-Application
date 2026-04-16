--Setup role
CREATE ROLE auth_owner LOGIN PASSWORD 'auth_owner';

--Create new database schema
CREATE SCHEMA IF NOT EXISTS auth AUTHORIZATION auth_owner;

-- Permissions
GRANT USAGE, CREATE ON SCHEMA auth TO auth_owner;
ALTER ROLE auth_owner SET search_path='auth';