export interface JournalLineDTOEnrichedResponse {
  id: number;
  amount: number;
  accountId: number;
  accountName: string;
  accountTypeName: string;
  accountDisplayName: string;
  lineAffectOnAccount: string;
  direction: string;
  notes: string;
}
