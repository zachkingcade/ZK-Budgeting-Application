import { AccountEnrichedObject } from "./AccountEnrichedObject";

export interface IGETAllAccountsResponse {
  accountsList: AccountEnrichedObject[];
}

export type GETAllAccountsResponse = IGETAllAccountsResponse;
