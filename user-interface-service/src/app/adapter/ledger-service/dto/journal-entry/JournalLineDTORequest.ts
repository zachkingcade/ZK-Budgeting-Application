export interface IJournalLineDTORequest {
  amount: number;
  accountId: number;
  direction: string;
  notes?: string;
}


export type JournalLineDTORequest = IJournalLineDTORequest;
