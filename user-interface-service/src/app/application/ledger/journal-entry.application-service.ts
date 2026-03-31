import { Injectable } from '@angular/core';
import { Observable, catchError, tap, throwError } from 'rxjs';
import { JournalEntryApi } from '../../adapter/ledger-service/api/journal-entry.api';
import { ApiResponse } from '../../adapter/ledger-service/dto/ApiResponse';
import { GETAllJournalEntrysRequest } from '../../adapter/ledger-service/dto/journal-entry/GETAllJournalEntrysRequest';
import { GETAllJournalEntrysResponse } from '../../adapter/ledger-service/dto/journal-entry/GETAllJournalEntrysResponse';
import { GETJournalEntryByIdResponse } from '../../adapter/ledger-service/dto/journal-entry/GETJournalEntryByIdResponse';
import { POSTCreateJournalEntryRequest } from '../../adapter/ledger-service/dto/journal-entry/POSTCreateJournalEntryRequest';
import { POSTCreateJournalEntryResponse } from '../../adapter/ledger-service/dto/journal-entry/POSTCreateJournalEntryResponse';
import { POSTUpdateJournalEntryRequest } from '../../adapter/ledger-service/dto/journal-entry/POSTUpdateJournalEntryRequest';
import { POSTUpdateJournalEntryResponse } from '../../adapter/ledger-service/dto/journal-entry/POSTUpdateJournalEntryResponse';
import { REMOVEJournalEntryResponse } from '../../adapter/ledger-service/dto/journal-entry/REMOVEJournalEntryResponse';
import { LedgerApplicationLoggerService } from './ledger-application-logger.service';

@Injectable({
  providedIn: 'root'
})
export class JournalEntryApplicationService {
  constructor(
    private readonly journalEntryApi: JournalEntryApi,
    private readonly logger: LedgerApplicationLoggerService
  ) {}

  getAll(request?: GETAllJournalEntrysRequest): Observable<ApiResponse<GETAllJournalEntrysResponse>> {
    const operation = 'JournalEntryApplicationService.getAll';
    const startTime = Date.now();
    this.logger.debug(`Starting ${operation}`, request);
    return this.journalEntryApi.getAll(request).pipe(
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
    const operation = 'JournalEntryApplicationService.getById';
    const startTime = Date.now();
    const context = { id };
    this.logger.debug(`Starting ${operation}`, context);
    return this.journalEntryApi.getById(id).pipe(
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

  create(request: POSTCreateJournalEntryRequest): Observable<ApiResponse<POSTCreateJournalEntryResponse>> {
    const operation = 'JournalEntryApplicationService.create';
    const startTime = Date.now();
    this.logger.debug(`Starting ${operation}`, request);
    return this.journalEntryApi.add(request).pipe(
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
    const operation = 'JournalEntryApplicationService.update';
    const startTime = Date.now();
    this.logger.debug(`Starting ${operation}`, request);
    return this.journalEntryApi.update(request).pipe(
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
    const operation = 'JournalEntryApplicationService.removeById';
    const startTime = Date.now();
    const context = { id };
    this.logger.debug(`Starting ${operation}`, context);
    return this.journalEntryApi.removeById(id).pipe(
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
