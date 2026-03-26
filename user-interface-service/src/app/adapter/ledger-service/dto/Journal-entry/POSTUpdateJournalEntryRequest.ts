import { JournalLineDTOUpdate } from "./JournalLineDTOUpdate";

export interface POSTUpdateJournalEntryRequest {
  id: number;
  description?: string;
  notes?: string;
  journalLines: JournalLineDTOUpdate[];
}