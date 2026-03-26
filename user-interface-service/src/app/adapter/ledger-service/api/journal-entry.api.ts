import { Injectable } from '@angular/core';
import { Observable, catchError, tap, throwError } from 'rxjs';
import { LedgerHttpClientService } from '../client/ledger-http-client.service';
import { LedgerAdapterLoggerService } from '../logging/ledger-adapter-logger.service';
import { ApiResponse } from '../dto/ApiResponse';
import { GETAllJournalEntrysRequest } from '../dto/Journal-entry/GETAllJournalEntrysRequest';
import { GETAllJournalEntrysResponse } from '../dto/Journal-entry/GETAllJournalEntrysResponse';
import { GETJournalEntryByIdResponse } from '../dto/Journal-entry/GETJournalEntryByIdResponse';
import { POSTCreateJournalEntryRequest } from '../dto/Journal-entry/POSTCreateJournalEntryRequest';
import { POSTCreateJournalEntryResponse } from '../dto/Journal-entry/POSTCreateJournalEntryResponse';
import { POSTUpdateJournalEntryRequest } from '../dto/Journal-entry/POSTUpdateJournalEntryRequest';
import { POSTUpdateJournalEntryResponse } from '../dto/Journal-entry/POSTUpdateJournalEntryResponse';
import { REMOVEJournalEntryResponse } from '../dto/Journal-entry/REMOVEJournalEntryResponse';

@Injectable({
  providedIn: 'root'
})
export class JournalEntryApi {
  constructor(
    private readonly ledgerClient: LedgerHttpClientService,
    private readonly logger: LedgerAdapterLoggerService
  ) {}

  getAll(request?: GETAllJournalEntrysRequest): Observable<ApiResponse<GETAllJournalEntrysResponse>> {
    const operation = 'JournalEntryApi.getAll /journalentry/all';
    const startTime = Date.now();
    this.logger.debug(`Starting ${operation}`, request);
    return this.ledgerClient.get<ApiResponse<GETAllJournalEntrysResponse>>('/journalentry/all', request).pipe(
      tap((response) => {
        this.logger.debug(`Ending ${operation} (${Date.now() - startTime}ms)`, {
          statusMessage: response.statusMessage,
          metaData: response.metaData
        });
      }),
      catchError((error) => {
        this.logger.error(`Failed ${operation} (${Date.now() - startTime}ms)`, error, request);
        return throwError(() => error);
      })
    );
  }

  getById(id: number): Observable<ApiResponse<GETJournalEntryByIdResponse>> {
    const operation = 'JournalEntryApi.getById /journalentry/byid/{id}';
    const startTime = Date.now();
    const context = { id };
    this.logger.debug(`Starting ${operation}`, context);
    return this.ledgerClient.get<ApiResponse<GETJournalEntryByIdResponse>>(`/journalentry/byid/${id}`).pipe(
      tap((response) => {
        this.logger.debug(`Ending ${operation} (${Date.now() - startTime}ms)`, {
          id,
          statusMessage: response.statusMessage,
          metaData: response.metaData
        });
      }),
      catchError((error) => {
        this.logger.error(`Failed ${operation} (${Date.now() - startTime}ms)`, error, context);
        return throwError(() => error);
      })
    );
  }

  add(request: POSTCreateJournalEntryRequest): Observable<ApiResponse<POSTCreateJournalEntryResponse>> {
    const operation = 'JournalEntryApi.add /journalentry/add';
    const startTime = Date.now();
    this.logger.debug(`Starting ${operation}`, request);
    return this.ledgerClient.post<ApiResponse<POSTCreateJournalEntryResponse>>('/journalentry/add', request).pipe(
      tap((response) => {
        this.logger.debug(`Ending ${operation} (${Date.now() - startTime}ms)`, {
          statusMessage: response.statusMessage,
          metaData: response.metaData
        });
      }),
      catchError((error) => {
        this.logger.error(`Failed ${operation} (${Date.now() - startTime}ms)`, error, request);
        return throwError(() => error);
      })
    );
  }

  update(request: POSTUpdateJournalEntryRequest): Observable<ApiResponse<POSTUpdateJournalEntryResponse>> {
    const operation = 'JournalEntryApi.update /journalentry/update';
    const startTime = Date.now();
    this.logger.debug(`Starting ${operation}`, request);
    return this.ledgerClient
      .post<ApiResponse<POSTUpdateJournalEntryResponse>>('/journalentry/update', request)
      .pipe(
        tap((response) => {
          this.logger.debug(`Ending ${operation} (${Date.now() - startTime}ms)`, {
            statusMessage: response.statusMessage,
            metaData: response.metaData
          });
        }),
        catchError((error) => {
          this.logger.error(`Failed ${operation} (${Date.now() - startTime}ms)`, error, request);
          return throwError(() => error);
        })
      );
  }

  removeById(id: number): Observable<ApiResponse<REMOVEJournalEntryResponse>> {
    const operation = 'JournalEntryApi.removeById /journalentry/remove/{id}';
    const startTime = Date.now();
    const context = { id };
    this.logger.debug(`Starting ${operation}`, context);
    return this.ledgerClient.delete<ApiResponse<REMOVEJournalEntryResponse>>(`/journalentry/remove/${id}`).pipe(
      tap((response) => {
        this.logger.debug(`Ending ${operation} (${Date.now() - startTime}ms)`, {
          id,
          statusMessage: response.statusMessage,
          metaData: response.metaData
        });
      }),
      catchError((error) => {
        this.logger.error(`Failed ${operation} (${Date.now() - startTime}ms)`, error, context);
        return throwError(() => error);
      })
    );
  }
}
