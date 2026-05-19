import { Injectable } from '@angular/core';
import { Observable, catchError, map, tap, throwError } from 'rxjs';
import { LedgerHttpClientService } from '../client/ledger-http-client.service';
import { LedgerAdapterLoggerService } from '../logging/ledger-adapter-logger.service';
import { ApiResponse } from '../dto/ApiResponse';
import {
  IReplayableJournalEntryDetail,
  IReplayableJournalEntryListItem,
  ISaveReplayableJournalEntryRequest,
} from '../dto/replayable-journal-entry/replayable-journal-entry.dto';

@Injectable({
  providedIn: 'root',
})
export class ReplayableJournalEntryApi {
  constructor(
    private readonly ledgerClient: LedgerHttpClientService,
    private readonly logger: LedgerAdapterLoggerService,
  ) {}

  list(): Observable<ApiResponse<IReplayableJournalEntryListItem[]>> {
    const operation = 'ReplayableJournalEntryApi.list';
    const start = Date.now();
    return this.ledgerClient.get<ApiResponse<IReplayableJournalEntryListItem[]>>('/replayable-journal-entries').pipe(
      tap((r) => this.logger.debug(`Ending ${operation} (${Date.now() - start}ms)`, r.statusMessage)),
      catchError((err) => {
        this.logger.error(`Failed ${operation} (${Date.now() - start}ms)`, err);
        return throwError(() => err);
      }),
    );
  }

  getById(id: number): Observable<ApiResponse<IReplayableJournalEntryDetail>> {
    const operation = 'ReplayableJournalEntryApi.getById';
    const start = Date.now();
    return this.ledgerClient
      .get<ApiResponse<IReplayableJournalEntryDetail>>(`/replayable-journal-entries/${id}`)
      .pipe(
        tap((r) => this.logger.debug(`Ending ${operation} (${Date.now() - start}ms)`, r.statusMessage)),
        catchError((err) => {
          this.logger.error(`Failed ${operation} (${Date.now() - start}ms)`, err, { id });
          return throwError(() => err);
        }),
      );
  }

  create(body: ISaveReplayableJournalEntryRequest): Observable<ApiResponse<IReplayableJournalEntryDetail>> {
    const operation = 'ReplayableJournalEntryApi.create';
    const start = Date.now();
    return this.ledgerClient.post<ApiResponse<IReplayableJournalEntryDetail>>('/replayable-journal-entries', body).pipe(
      tap((r) => this.logger.debug(`Ending ${operation} (${Date.now() - start}ms)`, r.statusMessage)),
      catchError((err) => {
        this.logger.error(`Failed ${operation} (${Date.now() - start}ms)`, err, body);
        return throwError(() => err);
      }),
    );
  }

  update(id: number, body: ISaveReplayableJournalEntryRequest): Observable<ApiResponse<IReplayableJournalEntryDetail>> {
    const operation = 'ReplayableJournalEntryApi.update';
    const start = Date.now();
    return this.ledgerClient
      .put<ApiResponse<IReplayableJournalEntryDetail>>(`/replayable-journal-entries/${id}`, body)
      .pipe(
        tap((r) => this.logger.debug(`Ending ${operation} (${Date.now() - start}ms)`, r.statusMessage)),
        catchError((err) => {
          this.logger.error(`Failed ${operation} (${Date.now() - start}ms)`, err, { id, body });
          return throwError(() => err);
        }),
      );
  }

  delete(id: number): Observable<void> {
    const operation = 'ReplayableJournalEntryApi.delete';
    const start = Date.now();
    return this.ledgerClient.delete<ApiResponse<null> | null>(`/replayable-journal-entries/${id}`).pipe(
      tap((r) => {
        if (r) {
          this.logger.debug(`Ending ${operation} (${Date.now() - start}ms)`, r.statusMessage);
        }
      }),
      catchError((err) => {
        this.logger.error(`Failed ${operation} (${Date.now() - start}ms)`, err, { id });
        return throwError(() => err);
      }),
      map(() => undefined),
    );
  }
}
