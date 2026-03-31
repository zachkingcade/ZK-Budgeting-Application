export interface IPOSTUpdateAccountResponse {
  accountId: number;
  typeId: number;
  description: string;
  active: boolean;
  notes: string;
}

export type POSTUpdateAccountResponse = IPOSTUpdateAccountResponse;
