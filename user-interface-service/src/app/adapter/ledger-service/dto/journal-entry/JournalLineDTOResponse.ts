export interface IJournalLineDTOResponse {
  id: number;
  amount: number;
  accountId: number;
  direction: string;
  notes: string;
}


export type JournalLineDTOResponse = IJournalLineDTOResponse;
