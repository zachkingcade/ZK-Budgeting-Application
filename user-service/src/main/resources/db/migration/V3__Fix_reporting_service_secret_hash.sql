-- Dev: This hash is the Spring Security BCryptPasswordEncoder test vector for plaintext "password"
-- Rotate via a new Flyway migration in production.
-- Original Attempt in V2 script failed, this one worked
UPDATE service_permissions
SET secret_hash = '$2a$00$9N8N35BVs5TLqGL3pspAte5OWWA2a2aZIs.EGp7At7txYakFERMue'
WHERE service_name = 'reporting-service';
