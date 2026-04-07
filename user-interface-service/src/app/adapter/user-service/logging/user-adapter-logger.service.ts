import { Injectable } from '@angular/core';

@Injectable({
  providedIn: 'root',
})
export class UserAdapterLoggerService {
  debug(message: string, context?: unknown): void {
    if (context !== undefined) {
      console.debug(`[UserAdapter] ${message}`, context);
      return;
    }

    console.debug(`[UserAdapter] ${message}`);
  }

  error(message: string, error: unknown, context?: unknown): void {
    if (context !== undefined) {
      console.error(`[UserAdapter] ${message}`, { context, error });
      return;
    }

    console.error(`[UserAdapter] ${message}`, error);
  }
}
