export interface IGETAccountByIdResponse {
  accountId: number;
  typeId: number;
  description: string;
  accountTypeName: string;
  accountDisplayName: string;
  accountBalance: number;
  active: boolean;
  notes: string;
}

export type GETAccountByIdResponse = IGETAccountByIdResponse;
