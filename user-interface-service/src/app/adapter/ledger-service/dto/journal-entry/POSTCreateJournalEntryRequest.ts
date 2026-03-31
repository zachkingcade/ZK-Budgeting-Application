import { JournalLineDTORequest } from "./JournalLineDTORequest";

export interface IPOSTCreateJournalEntryRequest {
  entryDate: string;
  description: string;
  notes?: string;
  journalLines: JournalLineDTORequest[];
}

export type POSTCreateJournalEntryRequest = IPOSTCreateJournalEntryRequest;
