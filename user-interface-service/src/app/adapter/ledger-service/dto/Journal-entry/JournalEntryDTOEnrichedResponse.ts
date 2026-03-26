import { JournalLineDTOEnrichedResponse } from "./JournalLineDTOEnrichedResponse";

export interface JournalEntryDTOEnrichedResponse {
  id: number;
  entryDate: string;
  description: string;
  notes: string;
  journalLines: JournalLineDTOEnrichedResponse[];
}
