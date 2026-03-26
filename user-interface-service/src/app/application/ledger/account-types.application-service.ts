import { Injectable } from '@angular/core';
import { Observable, catchError, tap, throwError } from 'rxjs';
import { AccountTypesApi } from '../../adapter/ledger-service/api/account-types.api';
import { ApiResponse } from '../../adapter/ledger-service/dto/ApiResponse';
import { GETAllAccountTypesRequest } from '../../adapter/ledger-service/dto/account-types/GETAllAccountTypesRequest';
import { GETAllAccountTypesResponse } from '../../adapter/ledger-service/dto/account-types/GETAllAccountTypesResponse';
import { GETAccountTypeByIdResponse } from '../../adapter/ledger-service/dto/account-types/GETAccountTypeByIdResponse';
import { POSTCreateAccountTypeRequest } from '../../adapter/ledger-service/dto/account-types/POSTCreateAccountTypeRequest';
import { POSTCreateAccountTypeResponse } from '../../adapter/ledger-service/dto/account-types/POSTCreateAccountTypeResponse';
import { POSTUpdateAccountTypeRequest } from '../../adapter/ledger-service/dto/account-types/POSTUpdateAccountTypeRequest';
import { POSTUpdateAccountTypeResponse } from '../../adapter/ledger-service/dto/account-types/POSTUpdateAccountTypeResponse';
import { LedgerApplicationLoggerService } from './ledger-application-logger.service';

@Injectable({
  providedIn: 'root'
})
export class AccountTypesApplicationService {
  constructor(
    private readonly accountTypesApi: AccountTypesApi,
    private readonly logger: LedgerApplicationLoggerService
  ) {}

  getAll(request?: GETAllAccountTypesRequest): Observable<ApiResponse<GETAllAccountTypesResponse>> {
    const operation = 'AccountTypesApplicationService.getAll';
    const startTime = Date.now();
    this.logger.debug(`Starting ${operation}`, request);
    return this.accountTypesApi.getAll(request).pipe(
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
    const operation = 'AccountTypesApplicationService.getById';
    const startTime = Date.now();
    const context = { id };
    this.logger.debug(`Starting ${operation}`, context);
    return this.accountTypesApi.getById(id).pipe(
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

  create(request: POSTCreateAccountTypeRequest): Observable<ApiResponse<POSTCreateAccountTypeResponse>> {
    const operation = 'AccountTypesApplicationService.create';
    const startTime = Date.now();
    this.logger.debug(`Starting ${operation}`, request);
    return this.accountTypesApi.add(request).pipe(
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
    const operation = 'AccountTypesApplicationService.update';
    const startTime = Date.now();
    this.logger.debug(`Starting ${operation}`, request);
    return this.accountTypesApi.update(request).pipe(
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
