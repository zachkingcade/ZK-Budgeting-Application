import { Injectable } from '@angular/core';
import { Observable, catchError, tap, throwError } from 'rxjs';
import { ReportsApi } from '../../adapter/reporting-service/api/reports.api';
import { ApiResponse } from '../../adapter/reporting-service/dto/ApiResponse';
import { ICatalogReport } from '../../adapter/reporting-service/dto/ICatalogReport';
import { IReportJobMetadata } from '../../adapter/reporting-service/dto/IReportJobMetadata';

@Injectable({
  providedIn: 'root',
})
export class ReportsApplicationService {
  constructor(private readonly reportsApi: ReportsApi) {}

  getCatalog(): Observable<ApiResponse<ICatalogReport[]>> {
    return this.reportsApi.getCatalog().pipe(
      tap(() => undefined),
      catchError((error) => throwError(() => error)),
    );
  }

  listReports(): Observable<ApiResponse<IReportJobMetadata[]>> {
    return this.reportsApi.listReports().pipe(
      tap(() => undefined),
      catchError((error) => throwError(() => error)),
    );
  }

  requestReport(reportType: string, parameters: unknown): Observable<ApiResponse<IReportJobMetadata>> {
    return this.reportsApi.requestReport(reportType, parameters).pipe(
      tap(() => undefined),
      catchError((error) => throwError(() => error)),
    );
  }

  getReport(id: number): Observable<ApiResponse<IReportJobMetadata>> {
    return this.reportsApi.getReport(id).pipe(
      tap(() => undefined),
      catchError((error) => throwError(() => error)),
    );
  }

  downloadPdf(id: number): Observable<Blob> {
    return this.reportsApi.downloadPdf(id).pipe(
      tap(() => undefined),
      catchError((error) => throwError(() => error)),
    );
  }
}
