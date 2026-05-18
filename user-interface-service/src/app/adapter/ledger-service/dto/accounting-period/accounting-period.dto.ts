export interface IClosedAccountingPeriodListItem {
  closedAccountingPeriodId: number;
  startDate: string;
  endDate: string;
  transactionCount: number;
}

export interface INextToCloseView {
  startDate: string;
  endDate: string;
  closable: boolean;
  closableAfter: string;
}

export interface IArchivedJournalEntryView {
  journalEntryId: number;
  entryDate: string;
  description: string;
  notes: string;
}

export interface ICloseAccountingPeriodRequest {
  startDate: string;
  endDate: string;
}
