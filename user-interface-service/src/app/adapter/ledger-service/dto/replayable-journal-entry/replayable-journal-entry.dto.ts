export interface IReplayableJournalEntryLine {
  amount: number;
  accountId: number;
  direction: 'C' | 'D';
}

export interface IReplayableJournalEntryListItem {
  replayableJournalEntryId: number;
  replayName: string;
  replayLineCount: number;
  createdAt: string;
  lastEditedAt: string;
}

export interface IReplayableJournalEntryDetail {
  replayableJournalEntryId: number;
  replayName: string;
  replayLineCount: number;
  createdAt: string;
  lastEditedAt: string;
  lines: IReplayableJournalEntryLine[];
}

export interface ISaveReplayableJournalEntryRequest {
  replayName: string;
  lines: IReplayableJournalEntryLine[];
  /** When true (default), debits must equal credits and at least two lines are required. */
  requireBalanced?: boolean;
}
