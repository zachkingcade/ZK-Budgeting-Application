ALTER TABLE replayable_journal_entries
    DROP CONSTRAINT chk_replayable_journal_entries_line_count;

ALTER TABLE replayable_journal_entries
    ADD CONSTRAINT chk_replayable_journal_entries_line_count CHECK (replay_line_count >= 1);
