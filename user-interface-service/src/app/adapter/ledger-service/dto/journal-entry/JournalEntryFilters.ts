export interface IJournalEntryFilters {
  dateAfter?: string;
  dateBefore?: string;
  descriptionContains?: string;
  notesContains?: string;
  accountTypes?: number[];
  accounts?: number[];
  searchContains?: string;
}


export type JournalEntryFilters = IJournalEntryFilters;
