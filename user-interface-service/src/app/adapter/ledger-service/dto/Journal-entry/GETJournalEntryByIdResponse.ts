import { JournalLineDTOEnrichedResponse } from "./JournalLineDTOEnrichedResponse";

export interface GETJournalEntryByIdResponse {
  id: number;
  entryDate: string;
  description: string;
  notes: string;
  journalLines: JournalLineDTOEnrichedResponse[];
}