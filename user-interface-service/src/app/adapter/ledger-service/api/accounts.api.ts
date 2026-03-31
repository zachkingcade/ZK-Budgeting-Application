import { Injectable } from '@angular/core';
import { Observable, catchError, tap, throwError } from 'rxjs';
import { LedgerHttpClientService } from '../client/ledger-http-client.service';
import { LedgerAdapterLoggerService } from '../logging/ledger-adapter-logger.service';
import { ApiResponse } from '../dto/ApiResponse';
import { GETAllAccountsRequest } from '../dto/account/GETAllAccountsRequest';
import { GETAllAccountsResponse } from '../dto/account/GETAllAccountsResponse';
import { GETAccountByIdResponse } from '../dto/account/GETAccountByIdResponse';
import { POSTCreateAccountRequest } from '../dto/account/POSTCreateAccountRequest';
import { POSTCreateAccountResponse } from '../dto/account/POSTCreateAccountResponse';
import { POSTUpdateAccountRequest } from '../dto/account/POSTUpdateAccountRequest';
import { POSTUpdateAccountResponse } from '../dto/account/POSTUpdateAccountResponse';

@Injectable({
  providedIn: 'root'
})
export class AccountsApi {
  constructor(
    private readonly ledgerClient: LedgerHttpClientService,
    private readonly logger: LedgerAdapterLoggerService
  ) {}

  getAll(request?: GETAllAccountsRequest): Observable<ApiResponse<GETAllAccountsResponse>> {
    const operation = 'AccountsApi.getAll /accounts/all';
    const startTime = Date.now();
    this.logger.debug(`Starting ${operation}`, request);
    const path: string = request ? '/accounts/all/filtered' : '/accounts/all';
    const source$ = request
      ? this.ledgerClient.post<ApiResponse<GETAllAccountsResponse>>(path, request)
      : this.ledgerClient.get<ApiResponse<GETAllAccountsResponse>>(path);

    return source$.pipe(
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
    const operation = 'AccountsApi.getById /accounts/byid/{id}';
    const startTime = Date.now();
    const context = { id };
    this.logger.debug(`Starting ${operation}`, context);
    return this.ledgerClient.get<ApiResponse<GETAccountByIdResponse>>(`/accounts/byid/${id}`).pipe(
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

  add(request: POSTCreateAccountRequest): Observable<ApiResponse<POSTCreateAccountResponse>> {
    const operation = 'AccountsApi.add /accounts/add';
    const startTime = Date.now();
    this.logger.debug(`Starting ${operation}`, request);
    return this.ledgerClient.post<ApiResponse<POSTCreateAccountResponse>>('/accounts/add', request).pipe(
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
    const operation = 'AccountsApi.update /accounts/update';
    const startTime = Date.now();
    this.logger.debug(`Starting ${operation}`, request);
    return this.ledgerClient.post<ApiResponse<POSTUpdateAccountResponse>>('/accounts/update', request).pipe(
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
