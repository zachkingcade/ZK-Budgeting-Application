-- account_types definition
ALTER TABLE account_types ADD COLUMN user_id BIGINT NOT NULL;
ALTER TABLE account_types ADD COLUMN system_account BOOLEAN NOT NULL DEFAULT FALSE;

-- chart_of_accounts definition
ALTER TABLE accounts ADD COLUMN user_id BIGINT NOT NULL;

-- ledger_transactions definition
ALTER TABLE journal_entries ADD COLUMN user_id BIGINT NOT NULL;
