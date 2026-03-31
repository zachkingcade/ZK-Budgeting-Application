export interface IAccountTypeFilters {
  descriptionContains?: string;
  notesContains?: string;
  accountClass?: number[];
  hideInactive?: boolean;
  hideActive?: boolean;
}


export type AccountTypeFilters = IAccountTypeFilters;
