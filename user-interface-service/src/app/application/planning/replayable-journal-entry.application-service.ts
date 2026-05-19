import { Injectable } from '@angular/core';
import { Observable, map } from 'rxjs';
import { ReplayableJournalEntryApi } from '../../adapter/ledger-service/api/replayable-journal-entry.api';
import {
  IReplayableJournalEntryDetail,
  IReplayableJournalEntryListItem,
  ISaveReplayableJournalEntryRequest,
} from '../../adapter/ledger-service/dto/replayable-journal-entry/replayable-journal-entry.dto';

@Injectable({
  providedIn: 'root',
})
export class ReplayableJournalEntryApplicationService {
  constructor(private readonly api: ReplayableJournalEntryApi) {}

  list(): Observable<IReplayableJournalEntryListItem[]> {
    return this.api.list().pipe(map((r) => r.data ?? []));
  }

  getById(id: number): Observable<IReplayableJournalEntryDetail> {
    return this.api.getById(id).pipe(map((r) => r.data!));
  }

  create(request: ISaveReplayableJournalEntryRequest): Observable<IReplayableJournalEntryDetail> {
    return this.api.create(this.withDefaultBalanced(request)).pipe(map((r) => r.data!));
  }

  update(id: number, request: ISaveReplayableJournalEntryRequest): Observable<IReplayableJournalEntryDetail> {
    return this.api.update(id, this.withDefaultBalanced(request)).pipe(map((r) => r.data!));
  }

  private withDefaultBalanced(request: ISaveReplayableJournalEntryRequest): ISaveReplayableJournalEntryRequest {
    return {
      ...request,
      requireBalanced: request.requireBalanced ?? true,
    };
  }

  delete(id: number): Observable<void> {
    return this.api.delete(id).pipe(map(() => undefined));
  }
}
