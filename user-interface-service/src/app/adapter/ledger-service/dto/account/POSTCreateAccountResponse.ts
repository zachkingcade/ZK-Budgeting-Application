export interface IPOSTCreateAccountResponse {
  accountId: number;
  typeId: number;
  description: string;
  active: boolean;
  notes: string;
}

export type POSTCreateAccountResponse = IPOSTCreateAccountResponse;
