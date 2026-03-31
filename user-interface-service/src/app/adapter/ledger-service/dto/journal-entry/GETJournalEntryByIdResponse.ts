import { JournalLineDTOEnrichedResponse } from "./JournalLineDTOEnrichedResponse";

export interface IGETJournalEntryByIdResponse {
  id: number;
  entryDate: string;
  description: string;
  notes: string;
  journalLines: JournalLineDTOEnrichedResponse[];
}

export type GETJournalEntryByIdResponse = IGETJournalEntryByIdResponse;
