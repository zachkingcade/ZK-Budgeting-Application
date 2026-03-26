import { Injectable } from '@angular/core';
import { Observable, catchError, tap, throwError } from 'rxjs';
import { AccountClassificationsApi } from '../../adapter/ledger-service/api/account-classifications.api';
import { ApiResponse } from '../../adapter/ledger-service/dto/ApiResponse';
import { GETAllAccountClassificationsResponse } from '../../adapter/ledger-service/dto/account-classification/GETAllAccountClassificationsResponse';
import { GETAccountClassificationByIdResponse } from '../../adapter/ledger-service/dto/account-classification/GETAccountClassificationByIdResponse';
import { LedgerApplicationLoggerService } from './ledger-application-logger.service';

@Injectable({
  providedIn: 'root'
})
export class AccountClassificationsApplicationService {
  constructor(
    private readonly accountClassificationsApi: AccountClassificationsApi,
    private readonly logger: LedgerApplicationLoggerService
  ) {}

  getAll(): Observable<ApiResponse<GETAllAccountClassificationsResponse>> {
    const operation = 'AccountClassificationsApplicationService.getAll';
    const startTime = Date.now();
    this.logger.debug(`Starting ${operation}`);
    return this.accountClassificationsApi.getAll().pipe(
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
    const operation = 'AccountClassificationsApplicationService.getById';
    const startTime = Date.now();
    const context = { id };
    this.logger.debug(`Starting ${operation}`, context);
    return this.accountClassificationsApi.getById(id).pipe(
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
