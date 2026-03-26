import { Injectable } from '@angular/core';
import { Observable, catchError, tap, throwError } from 'rxjs';
import { LedgerHttpClientService } from '../client/ledger-http-client.service';
import { LedgerAdapterLoggerService } from '../logging/ledger-adapter-logger.service';
import { ApiResponse } from '../dto/ApiResponse';
import { GETAllAccountTypesRequest } from '../dto/account-types/GETAllAccountTypesRequest';
import { GETAllAccountTypesResponse } from '../dto/account-types/GETAllAccountTypesResponse';
import { GETAccountTypeByIdResponse } from '../dto/account-types/GETAccountTypeByIdResponse';
import { POSTCreateAccountTypeRequest } from '../dto/account-types/POSTCreateAccountTypeRequest';
import { POSTCreateAccountTypeResponse } from '../dto/account-types/POSTCreateAccountTypeResponse';
import { POSTUpdateAccountTypeRequest } from '../dto/account-types/POSTUpdateAccountTypeRequest';
import { POSTUpdateAccountTypeResponse } from '../dto/account-types/POSTUpdateAccountTypeResponse';

@Injectable({
  providedIn: 'root'
})
export class AccountTypesApi {
  constructor(
    private readonly ledgerClient: LedgerHttpClientService,
    private readonly logger: LedgerAdapterLoggerService
  ) {}

  getAll(request?: GETAllAccountTypesRequest): Observable<ApiResponse<GETAllAccountTypesResponse>> {
    const operation = 'AccountTypesApi.getAll /accounttypes/all';
    const startTime = Date.now();
    this.logger.debug(`Starting ${operation}`, request);
    return this.ledgerClient.get<ApiResponse<GETAllAccountTypesResponse>>('/accounttypes/all', request).pipe(
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

  getById(id: number): Observable<ApiResponse<GETAccountTypeByIdResponse>> {
    const operation = 'AccountTypesApi.getById /accounttypes/byid/{id}';
    const startTime = Date.now();
    const context = { id };
    this.logger.debug(`Starting ${operation}`, context);
    return this.ledgerClient.get<ApiResponse<GETAccountTypeByIdResponse>>(`/accounttypes/byid/${id}`).pipe(
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

  add(request: POSTCreateAccountTypeRequest): Observable<ApiResponse<POSTCreateAccountTypeResponse>> {
    const operation = 'AccountTypesApi.add /accounttypes/add';
    const startTime = Date.now();
    this.logger.debug(`Starting ${operation}`, request);
    return this.ledgerClient.post<ApiResponse<POSTCreateAccountTypeResponse>>('/accounttypes/add', request).pipe(
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

  update(request: POSTUpdateAccountTypeRequest): Observable<ApiResponse<POSTUpdateAccountTypeResponse>> {
    const operation = 'AccountTypesApi.update /accounttypes/update';
    const startTime = Date.now();
    this.logger.debug(`Starting ${operation}`, request);
    return this.ledgerClient.post<ApiResponse<POSTUpdateAccountTypeResponse>>('/accounttypes/update', request).pipe(
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
