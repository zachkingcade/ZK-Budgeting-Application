import { Injectable } from '@angular/core';
import { Observable, map } from 'rxjs';
import { AccountingPeriodApi } from '../../adapter/ledger-service/api/accounting-period.api';
import {
  IArchivedJournalEntryView,
  ICloseAccountingPeriodRequest,
  IClosedAccountingPeriodListItem,
  INextToCloseView,
} from '../../adapter/ledger-service/dto/accounting-period/accounting-period.dto';

@Injectable({
  providedIn: 'root',
})
export class AccountingPeriodApplicationService {
  constructor(private readonly api: AccountingPeriodApi) {}

  listClosedPeriods(): Observable<IClosedAccountingPeriodListItem[]> {
    return this.api.listClosed().pipe(map((r) => r.data ?? []));
  }

  listArchivedEntries(closedPeriodId: number): Observable<IArchivedJournalEntryView[]> {
    return this.api.listArchivedJournalEntries(closedPeriodId).pipe(map((r) => r.data ?? []));
  }

  getNextToClose(): Observable<INextToCloseView> {
    return this.api.getNextToClose().pipe(map((r) => r.data!));
  }

  closePeriod(request: ICloseAccountingPeriodRequest): Observable<IClosedAccountingPeriodListItem> {
    return this.api.close(request).pipe(map((r) => r.data!));
  }
}
