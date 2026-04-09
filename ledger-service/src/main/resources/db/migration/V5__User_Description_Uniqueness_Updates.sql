-- account_types
ALTER TABLE account_types
    DROP CONSTRAINT IF EXISTS account_types_type_description_key;
ALTER TABLE account_types
    ADD CONSTRAINT uq_account_types_user_description
        UNIQUE (user_id, type_description);

-- accounts: enforce per-user uniqueness
ALTER TABLE accounts
    ADD CONSTRAINT uq_accounts_user_description
        UNIQUE (user_id, account_description);

