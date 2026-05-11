import { AccountEnrichedObject } from "./AccountEnrichedObject";

export interface AccountsGroupedByTypeObject {
  typeId: number;
  typeName: string;
  accounts: AccountEnrichedObject[];
}

export interface IGETAllAccountsResponse {
  accountsList: AccountEnrichedObject[];
  groups?: AccountsGroupedByTypeObject[] | null;
}

export type GETAllAccountsResponse = IGETAllAccountsResponse;
