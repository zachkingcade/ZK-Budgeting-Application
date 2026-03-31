import { AccountFilterObject } from "./AccountFilterObject";
import { SortObject } from "../SortObject";

export interface IGETAllAccountsRequest {
  sort?: SortObject;
  filters?: AccountFilterObject;
}

export type GETAllAccountsRequest = IGETAllAccountsRequest;
