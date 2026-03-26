import { JournalEntryFilters } from "./JournalEntryFilters";
import { SortObject } from "../SortObject";

export interface GETAllJournalEntrysRequest {
  sort?: SortObject;
  filters?: JournalEntryFilters;
}