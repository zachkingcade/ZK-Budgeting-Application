import { JournalLineDTORequest } from '../../journal-entry/JournalLineDTORequest';

export interface ApplyPendingTransactionItemRequest {
  pendingTransactionNumber: number;
  entryDate: string;
  description: string;
  notes?: string;
  journalLines: JournalLineDTORequest[];
}

