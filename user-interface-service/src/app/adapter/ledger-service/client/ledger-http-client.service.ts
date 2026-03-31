import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

@Injectable({
  providedIn: 'root'
})
export class LedgerHttpClientService {
  private readonly ledgerBaseUrl = 'http://localhost:8081';

  constructor(private readonly httpClient: HttpClient) {}

  get<T>(path: string): Observable<T> {
    return this.httpClient.get<T>(this.buildUrl(path));
  }

  post<T>(path: string, body: unknown): Observable<T> {
    return this.httpClient.post<T>(this.buildUrl(path), body);
  }

  delete<T>(path: string): Observable<T | null> {
    return this.httpClient.delete<T | null>(this.buildUrl(path));
  }

  private buildUrl(path: string): string {
    return `${this.ledgerBaseUrl}${path}`;
  }
}
