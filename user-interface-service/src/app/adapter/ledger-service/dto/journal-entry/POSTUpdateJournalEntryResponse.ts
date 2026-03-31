import { JournalLineDTOResponse } from "./JournalLineDTOResponse";

export interface IPOSTUpdateJournalEntryResponse {
  id: number;
  entryDate: string;
  description: string;
  notes: string;
  journalLines: JournalLineDTOResponse[];
}

export type POSTUpdateJournalEntryResponse = IPOSTUpdateJournalEntryResponse;
