import { JournalEntryFilters } from "./JournalEntryFilters";
import { SortObject } from "../SortObject";

export interface IGETAllJournalEntrysRequest {
  sort?: SortObject;
  filters?: JournalEntryFilters;
}

export type GETAllJournalEntrysRequest = IGETAllJournalEntrysRequest;