import { JournalLineDTOEnrichedResponse } from "./JournalLineDTOEnrichedResponse";

export interface IJournalEntryDTOEnrichedResponse {
  id: number;
  entryDate: string;
  description: string;
  notes: string;
  journalLines: JournalLineDTOEnrichedResponse[];
}


export type JournalEntryDTOEnrichedResponse = IJournalEntryDTOEnrichedResponse;
