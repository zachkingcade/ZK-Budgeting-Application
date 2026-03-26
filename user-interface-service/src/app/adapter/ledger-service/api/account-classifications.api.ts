import { Injectable } from '@angular/core';
import { Observable, catchError, tap, throwError } from 'rxjs';
import { LedgerHttpClientService } from '../client/ledger-http-client.service';
import { LedgerAdapterLoggerService } from '../logging/ledger-adapter-logger.service';
import { ApiResponse } from '../dto/ApiResponse';
import { GETAllAccountClassificationsResponse } from '../dto/account-classification/GETAllAccountClassificationsResponse';
import { GETAccountClassificationByIdResponse } from '../dto/account-classification/GETAccountClassificationByIdResponse';

@Injectable({
  providedIn: 'root'
})
export class AccountClassificationsApi {
  constructor(
    private readonly ledgerClient: LedgerHttpClientService,
    private readonly logger: LedgerAdapterLoggerService
  ) {}

  getAll(): Observable<ApiResponse<GETAllAccountClassificationsResponse>> {
    const operation = 'AccountClassificationsApi.getAll /accountclassifications/all';
    const startTime = Date.now();
    this.logger.debug(`Starting ${operation}`);
    return this.ledgerClient
      .get<ApiResponse<GETAllAccountClassificationsResponse>>('/accountclassifications/all')
      .pipe(
        tap((response) => {
          this.logger.debug(`Ending ${operation} (${Date.now() - startTime}ms)`, {
            statusMessage: response.statusMessage,
            metaData: response.metaData
          });
        }),
        catchError((error) => {
          this.logger.error(`Failed ${operation} (${Date.now() - startTime}ms)`, error);
          return throwError(() => error);
        })
      );
  }

  getById(id: number): Observable<ApiResponse<GETAccountClassificationByIdResponse>> {
    const operation = 'AccountClassificationsApi.getById /accountclassifications/byid/{id}';
    const startTime = Date.now();
    const context = { id };
    this.logger.debug(`Starting ${operation}`, context);
    return this.ledgerClient
      .get<ApiResponse<GETAccountClassificationByIdResponse>>(`/accountclassifications/byid/${id}`)
      .pipe(
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
