import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { LedgerHttpClientService } from '../client/ledger-http-client.service';
import { ApiResponse } from '../dto/ApiResponse';
import {
  IArchivedJournalEntryView,
  ICloseAccountingPeriodRequest,
  IClosedAccountingPeriodListItem,
  INextToCloseView,
} from '../dto/accounting-period/accounting-period.dto';

@Injectable({
  providedIn: 'root',
})
export class AccountingPeriodApi {
  constructor(private readonly ledgerClient: LedgerHttpClientService) {}

  listClosed(): Observable<ApiResponse<IClosedAccountingPeriodListItem[]>> {
    return this.ledgerClient.get<ApiResponse<IClosedAccountingPeriodListItem[]>>('/accounting-periods/closed');
  }

  listArchivedJournalEntries(
    closedPeriodId: number,
  ): Observable<ApiResponse<IArchivedJournalEntryView[]>> {
    return this.ledgerClient.get<ApiResponse<IArchivedJournalEntryView[]>>(
      `/accounting-periods/closed/${closedPeriodId}/journal-entries`,
    );
  }

  getNextToClose(): Observable<ApiResponse<INextToCloseView>> {
    return this.ledgerClient.get<ApiResponse<INextToCloseView>>('/accounting-periods/next-to-close');
  }

  close(body: ICloseAccountingPeriodRequest): Observable<ApiResponse<IClosedAccountingPeriodListItem>> {
    return this.ledgerClient.post<ApiResponse<IClosedAccountingPeriodListItem>>('/accounting-periods/close', body);
  }
}
