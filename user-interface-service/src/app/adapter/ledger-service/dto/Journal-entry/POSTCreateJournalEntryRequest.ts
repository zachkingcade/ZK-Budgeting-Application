import { JournalLineDTORequest } from "./JournalLineDTORequest";

export interface POSTCreateJournalEntryRequest {
  entryDate: string;
  description: string;
  notes?: string;
  journalLines: JournalLineDTORequest[];
}