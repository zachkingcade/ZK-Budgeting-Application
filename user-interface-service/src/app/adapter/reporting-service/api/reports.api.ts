import { Injectable } from '@angular/core';
import { Observable, catchError, tap, throwError } from 'rxjs';
import { ReportingHttpClientService } from '../client/reporting-http-client.service';
import { ApiResponse } from '../dto/ApiResponse';
import { ICatalogReport } from '../dto/ICatalogReport';
import { IReportJobMetadata } from '../dto/IReportJobMetadata';

@Injectable({
  providedIn: 'root',
})
export class ReportsApi {
  constructor(private readonly reportingClient: ReportingHttpClientService) {}

  getCatalog(): Observable<ApiResponse<ICatalogReport[]>> {
    return this.reportingClient.get<ApiResponse<ICatalogReport[]>>('/reports/catalog');
  }

  listReports(): Observable<ApiResponse<IReportJobMetadata[]>> {
    return this.reportingClient.get<ApiResponse<IReportJobMetadata[]>>('/reports');
  }

  requestReport(reportType: string, parameters: unknown): Observable<ApiResponse<IReportJobMetadata>> {
    return this.reportingClient.post<ApiResponse<IReportJobMetadata>>('/reports/requests', {
      reportType,
      parameters: parameters ?? {},
    });
  }

  getReport(id: number): Observable<ApiResponse<IReportJobMetadata>> {
    return this.reportingClient.get<ApiResponse<IReportJobMetadata>>(`/reports/${id}`);
  }

  downloadPdf(id: number): Observable<Blob> {
    return this.reportingClient.getBlob(`/reports/${id}/download`).pipe(
      tap(() => undefined),
      catchError((err) => throwError(() => err)),
    );
  }
}
