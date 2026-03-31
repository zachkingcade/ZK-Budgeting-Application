export interface IAccountFilterObject {
  descriptionContains?: string;
  notesContains?: string;
  accountTypes?: number[];
  hideInactive?: boolean;
  hideActive?: boolean;
}


export type AccountFilterObject = IAccountFilterObject;
