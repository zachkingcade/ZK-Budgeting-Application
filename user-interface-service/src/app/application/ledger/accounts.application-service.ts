import { Injectable } from '@angular/core';
import { Observable, catchError, tap, throwError } from 'rxjs';
import { AccountsApi } from '../../adapter/ledger-service/api/accounts.api';
import { ApiResponse } from '../../adapter/ledger-service/dto/ApiResponse';
import { GETAllAccountsRequest } from '../../adapter/ledger-service/dto/account/GETAllAccountsRequest';
import { GETAllAccountsResponse } from '../../adapter/ledger-service/dto/account/GETAllAccountsResponse';
import { GETAccountByIdResponse } from '../../adapter/ledger-service/dto/account/GETAccountByIdResponse';
import { POSTCreateAccountRequest } from '../../adapter/ledger-service/dto/account/POSTCreateAccountRequest';
import { POSTCreateAccountResponse } from '../../adapter/ledger-service/dto/account/POSTCreateAccountResponse';
import { POSTUpdateAccountRequest } from '../../adapter/ledger-service/dto/account/POSTUpdateAccountRequest';
import { POSTUpdateAccountResponse } from '../../adapter/ledger-service/dto/account/POSTUpdateAccountResponse';
import { LedgerApplicationLoggerService } from './ledger-application-logger.service';

@Injectable({
  providedIn: 'root'
})
export class AccountsApplicationService {
  constructor(
    private readonly accountsApi: AccountsApi,
    private readonly logger: LedgerApplicationLoggerService
  ) {}

  getAll(request?: GETAllAccountsRequest): Observable<ApiResponse<GETAllAccountsResponse>> {
    const operation = 'AccountsApplicationService.getAll';
    const startTime = Date.now();
    this.logger.debug(`Starting ${operation}`, request);
    return this.accountsApi.getAll(request).pipe(
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

  getById(id: number): Observable<ApiResponse<GETAccountByIdResponse>> {
    const operation = 'AccountsApplicationService.getById';
    const startTime = Date.now();
    const context = { id };
    this.logger.debug(`Starting ${operation}`, context);
    return this.accountsApi.getById(id).pipe(
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

  create(request: POSTCreateAccountRequest): Observable<ApiResponse<POSTCreateAccountResponse>> {
    const operation = 'AccountsApplicationService.create';
    const startTime = Date.now();
    this.logger.debug(`Starting ${operation}`, request);
    return this.accountsApi.add(request).pipe(
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

  update(request: POSTUpdateAccountRequest): Observable<ApiResponse<POSTUpdateAccountResponse>> {
    const operation = 'AccountsApplicationService.update';
    const startTime = Date.now();
    this.logger.debug(`Starting ${operation}`, request);
    return this.accountsApi.update(request).pipe(
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
}
