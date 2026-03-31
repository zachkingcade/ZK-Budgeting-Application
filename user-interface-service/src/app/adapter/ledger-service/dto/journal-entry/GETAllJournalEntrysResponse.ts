import { JournalEntryDTOEnrichedResponse } from "./JournalEntryDTOEnrichedResponse";

export interface IGETAllJournalEntrysResponse {
  journalEntryList: JournalEntryDTOEnrichedResponse[];
}


export type GETAllJournalEntrysResponse = IGETAllJournalEntrysResponse;
