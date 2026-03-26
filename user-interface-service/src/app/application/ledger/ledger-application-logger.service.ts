import { Injectable } from '@angular/core';

@Injectable({
  providedIn: 'root'
})
export class LedgerApplicationLoggerService {
  debug(message: string, context?: unknown): void {
    if (context !== undefined) {
      console.debug(`[LedgerApplication] ${message}`, context);
      return;
    }
    console.debug(`[LedgerApplication] ${message}`);
  }

  error(message: string, error: unknown, context?: unknown): void {
    if (context !== undefined) {
      console.error(`[LedgerApplication] ${message}`, { context, error });
      return;
    }
    console.error(`[LedgerApplication] ${message}`, error);
  }
}
