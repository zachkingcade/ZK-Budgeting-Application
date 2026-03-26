import { JournalLineDTOResponse } from "./JournalLineDTOResponse";

export interface POSTCreateJournalEntryResponse {
  id: number;
  entryDate: string;
  description: string;
  notes: string;
  journalLines: JournalLineDTOResponse[];
}