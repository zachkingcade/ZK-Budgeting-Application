import { Injectable } from '@angular/core';
import { Observable, catchError, tap, throwError } from 'rxjs';
import { PendingTransactionsApi } from '../../adapter/ledger-service/api/pending-transactions.api';
import { ApiResponse } from '../../adapter/ledger-service/dto/ApiResponse';
import { GETAllPendingTransactionsResponse } from '../../adapter/ledger-service/dto/pending-transaction/GETAllPendingTransactionsResponse';
import { POSTImportPendingTransactionsResponse } from '../../adapter/ledger-service/dto/pending-transaction/POSTImportPendingTransactionsResponse';
import { REMOVEPendingTransactionResponse } from '../../adapter/ledger-service/dto/pending-transaction/REMOVEPendingTransactionResponse';
import { POSTApplyPendingTransactionsRequest } from '../../adapter/ledger-service/dto/pending-transaction/apply/POSTApplyPendingTransactionsRequest';
import { POSTApplyPendingTransactionsResponse } from '../../adapter/ledger-service/dto/pending-transaction/apply/POSTApplyPendingTransactionsResponse';
import { LedgerApplicationLoggerService } from './ledger-application-logger.service';

@Injectable({
  providedIn: 'root',
})
export class PendingTransactionsApplicationService {
  constructor(
    private readonly api: PendingTransactionsApi,
    private readonly logger: LedgerApplicationLoggerService,
  ) {}

  getAll(): Observable<ApiResponse<GETAllPendingTransactionsResponse>> {
    const operation = 'PendingTransactionsApplicationService.getAll';
    const startTime = Date.now();
    this.logger.debug(`Starting ${operation}`);
    return this.api.getAll().pipe(
      tap((response) => {
        this.logger.debug(`Ending ${operation} (${Date.now() - startTime}ms)`, {
          statusMessage: response.statusMessage,
          metaData: response.metaData,
        });
      }),
      catchError((error) => {
        this.logger.error(`Failed ${operation} (${Date.now() - startTime}ms)`, error);
        return throwError(() => error);
      }),
    );
  }

  removeById(transactionNumber: number): Observable<ApiResponse<REMOVEPendingTransactionResponse>> {
    const operation = 'PendingTransactionsApplicationService.removeById';
    const startTime = Date.now();
    const context = { transactionNumber };
    this.logger.debug(`Starting ${operation}`, context);
    return this.api.removeById(transactionNumber).pipe(
      tap((response) => {
        this.logger.debug(`Ending ${operation} (${Date.now() - startTime}ms)`, {
          statusMessage: response.statusMessage,
          metaData: response.metaData,
        });
      }),
      catchError((error) => {
        this.logger.error(`Failed ${operation} (${Date.now() - startTime}ms)`, error, context);
        return throwError(() => error);
      }),
    );
  }

  import(formatId: number, file: File): Observable<ApiResponse<POSTImportPendingTransactionsResponse>> {
    const operation = 'PendingTransactionsApplicationService.import';
    const startTime = Date.now();
    const context = { formatId, fileName: file?.name };
    this.logger.debug(`Starting ${operation}`, context);
    return this.api.import(formatId, file).pipe(
      tap((response) => {
        this.logger.debug(`Ending ${operation} (${Date.now() - startTime}ms)`, {
          statusMessage: response.statusMessage,
          metaData: response.metaData,
        });
      }),
      catchError((error) => {
        this.logger.error(`Failed ${operation} (${Date.now() - startTime}ms)`, error, context);
        return throwError(() => error);
      }),
    );
  }

  apply(request: POSTApplyPendingTransactionsRequest): Observable<ApiResponse<POSTApplyPendingTransactionsResponse>> {
    const operation = 'PendingTransactionsApplicationService.apply';
    const startTime = Date.now();
    this.logger.debug(`Starting ${operation}`, request);
    return this.api.apply(request).pipe(
      tap((response) => {
        this.logger.debug(`Ending ${operation} (${Date.now() - startTime}ms)`, {
          statusMessage: response.statusMessage,
          metaData: response.metaData,
        });
      }),
      catchError((error) => {
        this.logger.error(`Failed ${operation} (${Date.now() - startTime}ms)`, error, request);
        return throwError(() => error);
      }),
    );
  }
}

