export interface PendingTransactionObject {
  transactionNumber: number;
  transactionDate: string;
  description: string;
  amount: number; // minor units
  notes: string;
}

