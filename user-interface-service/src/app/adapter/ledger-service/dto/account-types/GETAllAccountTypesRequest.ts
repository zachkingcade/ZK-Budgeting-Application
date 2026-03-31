import { AccountTypeFilters } from "./AccountTypeFilters";
import { SortObject } from "../SortObject";

export interface IGETAllAccountTypesRequest {
  sort?: SortObject;
  filters?: AccountTypeFilters;
}

export type GETAllAccountTypesRequest = IGETAllAccountTypesRequest;
