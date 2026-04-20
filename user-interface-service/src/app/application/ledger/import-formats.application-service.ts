import { Injectable } from '@angular/core';
import { Observable, catchError, tap, throwError } from 'rxjs';
import { ImportFormatsApi } from '../../adapter/ledger-service/api/import-formats.api';
import { ApiResponse } from '../../adapter/ledger-service/dto/ApiResponse';
import { GETAllImportFormatsResponse } from '../../adapter/ledger-service/dto/import-format/GETAllImportFormatsResponse';
import { LedgerApplicationLoggerService } from './ledger-application-logger.service';

@Injectable({
  providedIn: 'root',
})
export class ImportFormatsApplicationService {
  constructor(
    private readonly api: ImportFormatsApi,
    private readonly logger: LedgerApplicationLoggerService,
  ) {}

  getAll(): Observable<ApiResponse<GETAllImportFormatsResponse>> {
    const operation = 'ImportFormatsApplicationService.getAll';
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
}

