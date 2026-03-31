export interface IJournalEntryFilters {
  dateAfter?: string;
  dateBefore?: string;
  descriptionContains?: string;
  notesContains?: string;
  accountTypes?: number[];
  accounts?: number[];
}


export type JournalEntryFilters = IJournalEntryFilters;
