-- Allow same account description per user when account types differ
ALTER TABLE accounts
    DROP CONSTRAINT IF EXISTS uq_accounts_user_description;

ALTER TABLE accounts
    ADD CONSTRAINT uq_accounts_user_type_description
        UNIQUE (user_id, account_type_id, account_description);
