import { Injectable } from '@angular/core';
import { Observable, catchError, map, tap, throwError } from 'rxjs';
import { LedgerHttpClientService } from '../client/ledger-http-client.service';
import { LedgerAdapterLoggerService } from '../logging/ledger-adapter-logger.service';
import { ApiResponse } from '../dto/ApiResponse';
import { GETAllPendingTransactionsResponse } from '../dto/pending-transaction/GETAllPendingTransactionsResponse';
import { REMOVEPendingTransactionResponse } from '../dto/pending-transaction/REMOVEPendingTransactionResponse';
import { POSTImportPendingTransactionsResponse } from '../dto/pending-transaction/POSTImportPendingTransactionsResponse';
import { POSTApplyPendingTransactionsRequest } from '../dto/pending-transaction/apply/POSTApplyPendingTransactionsRequest';
import { POSTApplyPendingTransactionsResponse } from '../dto/pending-transaction/apply/POSTApplyPendingTransactionsResponse';

@Injectable({
  providedIn: 'root',
})
export class PendingTransactionsApi {
  constructor(
    private readonly ledgerClient: LedgerHttpClientService,
    private readonly logger: LedgerAdapterLoggerService,
  ) {}

  getAll(): Observable<ApiResponse<GETAllPendingTransactionsResponse>> {
    const operation = 'PendingTransactionsApi.getAll /pendingtransactions/all';
    const startTime = Date.now();
    this.logger.debug(`Starting ${operation}`);
    return this.ledgerClient.get<ApiResponse<GETAllPendingTransactionsResponse>>('/pendingtransactions/all').pipe(
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
    const operation = 'PendingTransactionsApi.removeById /pendingtransactions/remove/{id}';
    const startTime = Date.now();
    const context = { transactionNumber };
    this.logger.debug(`Starting ${operation}`, context);
    return this.ledgerClient
      .delete<ApiResponse<REMOVEPendingTransactionResponse>>(`/pendingtransactions/remove/${transactionNumber}`)
      .pipe(
        map((body) => {
          // Some DELETE handlers return no body; normalize to a consistent ApiResponse shape.
          if (body != null) {
            return body;
          }
          return {
            statusMessage: 'Removed',
            metaData: { requestDate: '', requestTime: '' },
            data: { transactionNumber },
          };
        }),
        tap((response) => {
          this.logger.debug(`Ending ${operation} (${Date.now() - startTime}ms)`, {
            transactionNumber,
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
    const operation = 'PendingTransactionsApi.import /pendingtransactions/import';
    const startTime = Date.now();
    const context = { formatId, fileName: file?.name };
    this.logger.debug(`Starting ${operation}`, context);

    const formData = new FormData();
    formData.set('formatId', String(formatId));
    formData.set('file', file);

    return this.ledgerClient.postForm<ApiResponse<POSTImportPendingTransactionsResponse>>('/pendingtransactions/import', formData).pipe(
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
    const operation = 'PendingTransactionsApi.apply /pendingtransactions/apply';
    const startTime = Date.now();
    this.logger.debug(`Starting ${operation}`, request);
    return this.ledgerClient.post<ApiResponse<POSTApplyPendingTransactionsResponse>>('/pendingtransactions/apply', request).pipe(
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

