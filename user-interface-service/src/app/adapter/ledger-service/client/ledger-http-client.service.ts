import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

@Injectable({
  providedIn: 'root'
})
export class LedgerHttpClientService {
  private readonly ledgerBaseUrl = 'http://localhost:8081';

  constructor(private readonly httpClient: HttpClient) {}

  get<T>(path: string, body?: unknown): Observable<T> {
    const url = this.buildUrl(path);
    if (body === undefined) {
      return this.httpClient.get<T>(url);
    }

    return this.httpClient.request<T>('GET', url, { body });
  }

  post<T>(path: string, body: unknown): Observable<T> {
    return this.httpClient.post<T>(this.buildUrl(path), body);
  }

  delete<T>(path: string): Observable<T> {
    return this.httpClient.delete<T>(this.buildUrl(path));
  }

  private buildUrl(path: string): string {
    return `${this.ledgerBaseUrl}${path}`;
  }
}
