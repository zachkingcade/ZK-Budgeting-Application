-- Allow fractional minor units per day (internal precision); display layer still rounds for users.

ALTER TABLE bp_budgets
    ALTER COLUMN target_amount_per_day_minor TYPE NUMERIC(30, 12)
    USING (target_amount_per_day_minor::numeric);
