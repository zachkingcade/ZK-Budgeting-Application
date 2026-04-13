import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../../environments/environment';

@Injectable({
  providedIn: 'root',
})
export class ReportingHttpClientService {
  private readonly reportingBaseUrl: string = environment.reportingApiUrl;

  constructor(private readonly httpClient: HttpClient) {}

  get<T>(path: string): Observable<T> {
    return this.httpClient.get<T>(this.buildUrl(path));
  }

  post<T>(path: string, body: unknown): Observable<T> {
    return this.httpClient.post<T>(this.buildUrl(path), body);
  }

  getBlob(path: string): Observable<Blob> {
    return this.httpClient.get(this.buildUrl(path), { responseType: 'blob' });
  }

  private buildUrl(path: string): string {
    return `${this.reportingBaseUrl}${path}`;
  }
}
