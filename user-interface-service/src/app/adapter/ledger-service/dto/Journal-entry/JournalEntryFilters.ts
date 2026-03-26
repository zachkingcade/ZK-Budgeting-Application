export interface JournalEntryFilters {
  dateAfter?: string;
  dateBefore?: string;
  descriptionContains?: string;
  notesContains?: string;
  accountTypes?: number[];
  accounts?: number[];
}
