import { AccountFilterObject } from "./AccountFilterObject";
import { SortObject } from "../SortObject";

export interface IGETAllAccountsRequest {
  sort?: SortObject;
  filters?: AccountFilterObject;
  /** When true, response includes `groups` and `accountsList` in type order. Always send true or false so the ledger deserializes reliably. */
  groupByType?: boolean;
}

export type GETAllAccountsRequest = IGETAllAccountsRequest;
