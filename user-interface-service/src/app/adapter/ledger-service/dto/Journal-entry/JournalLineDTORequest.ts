export interface JournalLineDTORequest {
  amount: number;
  accountId: number;
  direction: string;
  notes?: string;
}
