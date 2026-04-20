import { Injectable } from '@angular/core';
import { Observable, catchError, tap, throwError } from 'rxjs';
import { LedgerHttpClientService } from '../client/ledger-http-client.service';
import { LedgerAdapterLoggerService } from '../logging/ledger-adapter-logger.service';
import { ApiResponse } from '../dto/ApiResponse';
import { GETAllImportFormatsResponse } from '../dto/import-format/GETAllImportFormatsResponse';

@Injectable({
  providedIn: 'root',
})
export class ImportFormatsApi {
  constructor(
    private readonly ledgerClient: LedgerHttpClientService,
    private readonly logger: LedgerAdapterLoggerService,
  ) {}

  getAll(): Observable<ApiResponse<GETAllImportFormatsResponse>> {
    const operation = 'ImportFormatsApi.getAll /importformats/all';
    const startTime = Date.now();
    this.logger.debug(`Starting ${operation}`);
    return this.ledgerClient.get<ApiResponse<GETAllImportFormatsResponse>>('/importformats/all').pipe(
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
}

