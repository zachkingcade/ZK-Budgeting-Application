-- Update seeded USAA import format dateFormat to match actual export files (YYYY-MM-DD)
UPDATE ledger.import_formats
SET format_details = jsonb_set(format_details, '{dateFormat}', '"yyyy-MM-dd"'::jsonb, true)
WHERE format_id = 1;

