import { Injectable } from '@angular/core';

@Injectable({
  providedIn: 'root'
})
export class LedgerAdapterLoggerService {
  debug(message: string, context?: unknown): void {
    if (context !== undefined) {
      console.debug(`[LedgerAdapter] ${message}`, context);
      return;
    }

    console.debug(`[LedgerAdapter] ${message}`);
  }

  error(message: string, error: unknown, context?: unknown): void {
    if (context !== undefined) {
      console.error(`[LedgerAdapter] ${message}`, { context, error });
      return;
    }

    console.error(`[LedgerAdapter] ${message}`, error);
  }
}
