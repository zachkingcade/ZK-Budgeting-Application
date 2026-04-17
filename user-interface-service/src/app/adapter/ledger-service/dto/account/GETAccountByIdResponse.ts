export interface IGETAccountByIdResponse {
  accountId: number;
  typeId: number;
  description: string;
  accountTypeName: string;
  accountDisplayName: string;
  accountBalance: number;
  active: boolean;
  notes: string;
  creditEffect: string;
  debitEffect: string;
}

export type GETAccountByIdResponse = IGETAccountByIdResponse;
