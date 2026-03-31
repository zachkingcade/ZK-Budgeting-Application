import { JournalLineDTOResponse } from "./JournalLineDTOResponse";

export interface IPOSTCreateJournalEntryResponse {
  id: number;
  entryDate: string;
  description: string;
  notes: string;
  journalLines: JournalLineDTOResponse[];
}

export type POSTCreateJournalEntryResponse = IPOSTCreateJournalEntryResponse;
