--------------------------------------------------------------------------------------------------------------------------------
-- Asset Classification Account Types
INSERT INTO ledger.account_types(type_id, type_description, classification_id, notes, type_active, user_id, system_account)
VALUES(1, 'Cash', 1, 'Checking/Cash on hand', true, -1, true);

INSERT INTO ledger.account_types(type_id, type_description, classification_id, notes, type_active, user_id, system_account)
VALUES(2, 'Savings', 1, 'Money stored away or saved over time', true, -1, true);

INSERT INTO ledger.account_types(type_id, type_description, classification_id, notes, type_active, user_id, system_account)
VALUES(3, 'Investment', 1, '401k, brokerage, IRA, etc.', true, -1, true);

INSERT INTO ledger.account_types(type_id, type_description, classification_id, notes, type_active, user_id, system_account)
VALUES(4, 'Property', 1, 'House, car, major owned assets if you want net worth reporting.', true, -1, true);

INSERT INTO ledger.account_types(type_id, type_description, classification_id, notes, type_active, user_id, system_account)
VALUES(5, 'Budget', 1, 'Reserved funds/Money with a job, spent regularly', true, -1, true);

INSERT INTO ledger.account_types(type_id, type_description, classification_id, notes, type_active, user_id, system_account)
VALUES(6, 'Fund', 1, 'Reserved funds that build over time, spent infrequently', true, -1, true);
--------------------------------------------------------------------------------------------------------------------------------
--------------------------------------------------------------------------------------------------------------------------------
-- Liability Classification Account Types
INSERT INTO ledger.account_types(type_id, type_description, classification_id, notes, type_active, user_id, system_account)
VALUES(7, 'Debt', 2, 'Money owed', true, -1, true);

INSERT INTO ledger.account_types(type_id, type_description, classification_id, notes, type_active, user_id, system_account)
VALUES(8, 'Credit Card', 2, 'Temporary Debt Accounts', true, -1, true);

INSERT INTO ledger.account_types(type_id, type_description, classification_id, notes, type_active, user_id, system_account)
VALUES(9, 'Loan', 2, 'Mortgage, auto loan, personal loan, student loan.', true, -1, true);
--------------------------------------------------------------------------------------------------------------------------------
--------------------------------------------------------------------------------------------------------------------------------
-- Equity Classification Account Types
INSERT INTO ledger.account_types(type_id, type_description, classification_id, notes, type_active, user_id, system_account)
VALUES(10, 'Starting Equity', 3, 'Equity balanced at the start of the system', true, -1, true);
--------------------------------------------------------------------------------------------------------------------------------
--------------------------------------------------------------------------------------------------------------------------------
-- Revenue Classification Account Types
INSERT INTO ledger.account_types(type_id, type_description, classification_id, notes, type_active, user_id, system_account)
VALUES(11, 'Income', 4, 'Salary, side income, gifts, refunds, bonus, etc.', true, -1, true);
--------------------------------------------------------------------------------------------------------------------------------
--------------------------------------------------------------------------------------------------------------------------------
-- Expense Classification Account Types
INSERT INTO ledger.account_types(type_id, type_description, classification_id, notes, type_active, user_id, system_account)
VALUES(12, 'Bills', 5, 'Predictable recurring obligations.', true, -1, true);

INSERT INTO ledger.account_types(type_id, type_description, classification_id, notes, type_active, user_id, system_account)
VALUES(13, 'Living Expense', 5, 'Groceries, gas, household basics, healthcare basics, etc.', true, -1, true);

INSERT INTO ledger.account_types(type_id, type_description, classification_id, notes, type_active, user_id, system_account)
VALUES(14, 'Discretionary Expense', 5, 'Dining out, hobbies, entertainment, impulse nonsense, the tiny treat industrial complex.', true, -1, true);
--------------------------------------------------------------------------------------------------------------------------------