import { JournalLineDTOUpdate } from "./JournalLineDTOUpdate";

export interface IPOSTUpdateJournalEntryRequest {
  id: number;
  description?: string;
  notes?: string;
  journalLines: JournalLineDTOUpdate[];
}

export type POSTUpdateJournalEntryRequest = IPOSTUpdateJournalEntryRequest;
