export interface IAccountFilterObject {
  descriptionContains?: string;
  notesContains?: string;
  accountTypes?: number[];
  hideInactive?: boolean;
  hideActive?: boolean;
  searchContains?: string;
}


export type AccountFilterObject = IAccountFilterObject;
