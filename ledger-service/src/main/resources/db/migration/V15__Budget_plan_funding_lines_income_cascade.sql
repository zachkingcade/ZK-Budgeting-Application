ALTER TABLE bp_funding_lines
  DROP CONSTRAINT fk_bp_funding_lines_income;

ALTER TABLE bp_funding_lines
  ADD CONSTRAINT fk_bp_funding_lines_income
    FOREIGN KEY (bp_income_id) REFERENCES bp_incomes(bp_income_id) ON DELETE CASCADE;
