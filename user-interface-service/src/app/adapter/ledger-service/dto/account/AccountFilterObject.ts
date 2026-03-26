export interface AccountFilterObject {
  descriptionContains?: string;
  notesContains?: string;
  accountTypes?: number[];
  hideInactive?: boolean;
  hideActive?: boolean;
}
