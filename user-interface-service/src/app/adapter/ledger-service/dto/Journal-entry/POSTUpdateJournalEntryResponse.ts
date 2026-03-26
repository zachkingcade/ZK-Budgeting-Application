import { JournalLineDTOResponse } from "./JournalLineDTOResponse";

export interface POSTUpdateJournalEntryResponse {
  id: number;
  entryDate: string;
  description: string;
  notes: string;
  journalLines: JournalLineDTOResponse[];
}