export interface AccountTypeFilters {
  descriptionContains?: string;
  notesContains?: string;
  accountClass?: number[];
  hideInactive?: boolean;
  hideActive?: boolean;
}
