import { AccountTypeObject } from "./AccountTypeObject";

export interface IGETAllAccountTypesResponse {
  accountTypeList: AccountTypeObject[];
}

export type GETAllAccountTypesResponse = IGETAllAccountTypesResponse;
