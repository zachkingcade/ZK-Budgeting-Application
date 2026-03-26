import { AccountTypeFilters } from "./AccountTypeFilters";
import { SortObject } from "../SortObject";

export interface GETAllAccountTypesRequest {
  sort?: SortObject;
  filters?: AccountTypeFilters;
}