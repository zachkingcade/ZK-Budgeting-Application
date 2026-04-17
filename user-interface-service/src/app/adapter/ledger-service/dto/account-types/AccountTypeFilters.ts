export interface IAccountTypeFilters {
  descriptionContains?: string;
  notesContains?: string;
  accountClass?: number[];
  hideInactive?: boolean;
  hideActive?: boolean;
  searchContains?: string;
  hideSystemAccounts?: boolean;
}


export type AccountTypeFilters = IAccountTypeFilters;
