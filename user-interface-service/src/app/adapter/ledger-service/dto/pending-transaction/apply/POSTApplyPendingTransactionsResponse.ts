import { ApplyPendingTransactionsFailureObject } from './ApplyPendingTransactionsFailureObject';
import { ApplyPendingTransactionsSuccessObject } from './ApplyPendingTransactionsSuccessObject';

export interface POSTApplyPendingTransactionsResponse {
  successCount: number;
  failureCount: number;
  succeeded: ApplyPendingTransactionsSuccessObject[];
  failed: ApplyPendingTransactionsFailureObject[];
}

