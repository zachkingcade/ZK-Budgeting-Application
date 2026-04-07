import { Injectable } from '@angular/core';

export interface ICachedSession {
  username: string;
  sessionToken: string;
  sessionExpiresAt: string | null;
}

@Injectable({
  providedIn: 'root',
})
export class SessionStorageService {
  private static readonly USERNAME_KEY: string = 'auth.username';
  private static readonly SESSION_TOKEN_KEY: string = 'auth.sessionToken';
  private static readonly SESSION_EXPIRES_AT_KEY: string = 'auth.sessionExpiresAt';

  getCachedSession(): ICachedSession | null {
    const username: string | null = localStorage.getItem(SessionStorageService.USERNAME_KEY);
    const sessionToken: string | null = localStorage.getItem(SessionStorageService.SESSION_TOKEN_KEY);
    const sessionExpiresAt: string | null = localStorage.getItem(SessionStorageService.SESSION_EXPIRES_AT_KEY);

    if (!username || !sessionToken) {
      return null;
    }

    return {
      username,
      sessionToken,
      sessionExpiresAt,
    };
  }

  setCachedSession(session: ICachedSession): void {
    localStorage.setItem(SessionStorageService.USERNAME_KEY, session.username);
    localStorage.setItem(SessionStorageService.SESSION_TOKEN_KEY, session.sessionToken);

    if (session.sessionExpiresAt != null) {
      localStorage.setItem(SessionStorageService.SESSION_EXPIRES_AT_KEY, session.sessionExpiresAt);
      return;
    }

    localStorage.removeItem(SessionStorageService.SESSION_EXPIRES_AT_KEY);
  }

  clearCachedSession(): void {
    localStorage.removeItem(SessionStorageService.USERNAME_KEY);
    localStorage.removeItem(SessionStorageService.SESSION_TOKEN_KEY);
    localStorage.removeItem(SessionStorageService.SESSION_EXPIRES_AT_KEY);
  }
}
