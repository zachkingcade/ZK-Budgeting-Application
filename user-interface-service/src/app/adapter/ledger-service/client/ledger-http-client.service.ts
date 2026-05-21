import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../../environments/environment';

@Injectable({
  providedIn: 'root'
})
export class LedgerHttpClientService {
  private readonly ledgerBaseUrl = environment.ledgerApiUrl;

  constructor(private readonly httpClient: HttpClient) {}

  get<T>(path: string): Observable<T> {
    return this.httpClient.get<T>(this.buildUrl(path));
  }

  post<T>(path: string, body: unknown): Observable<T> {
    return this.httpClient.post<T>(this.buildUrl(path), body);
  }

  patch<T>(path: string, body: unknown): Observable<T> {
    return this.httpClient.patch<T>(this.buildUrl(path), body);
  }

  put<T>(path: string, body: unknown): Observable<T> {
    return this.httpClient.put<T>(this.buildUrl(path), body);
  }

  postForm<T>(path: string, formData: FormData): Observable<T> {
    return this.httpClient.post<T>(this.buildUrl(path), formData);
  }

  delete<T>(path: string): Observable<T | null> {
    return this.httpClient.delete<T | null>(this.buildUrl(path));
  }

  private buildUrl(path: string): string {
    return `${this.ledgerBaseUrl}${path}`;
  }
}
