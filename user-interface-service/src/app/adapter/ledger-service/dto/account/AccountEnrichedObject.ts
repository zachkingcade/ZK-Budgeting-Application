export interface IAccountEnrichedObject {
  accountId: number;
  typeId: number;
  description: string;
  accountTypeName: string;
  accountDisplayName: string;
  accountBalance: number;
  active: boolean;
  notes: string;
}


export type AccountEnrichedObject = IAccountEnrichedObject;
