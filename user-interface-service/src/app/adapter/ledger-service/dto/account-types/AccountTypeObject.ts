export interface IAccountTypeObject {
  id: number;
  classificationId: number;
  description: string;
  active: boolean;
  notes: string;
  systemAccount: boolean;
}


export type AccountTypeObject = IAccountTypeObject;
