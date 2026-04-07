import { Injectable } from '@angular/core';
import { Observable, catchError, map, of, switchMap, throwError } from 'rxjs';
import { UserAuthApi } from '../../adapter/user-service/api/user-auth.api';
import { SessionStorageService } from '../../adapter/auth/session-storage.service';
import { LoginUserRequestDto } from '../../adapter/user-service/dto/user/login-user-request.dto';
import { RegisterUserRequestDto } from '../../adapter/user-service/dto/user/register-user-request.dto';
import { LogoutUserRequestDto } from '../../adapter/user-service/dto/user/logout-user-request.dto';
import { RefreshLoginRequestDto } from '../../adapter/user-service/dto/user/refresh-login-request.dto';

export interface IAuthSnapshot {
  username: string | null;
  sessionToken: string | null;
  accessToken: string | null;
  accessTokenExpiresAt: string | null;
}

@Injectable({
  providedIn: 'root',
})
export class AuthManagerService {
  private accessToken: string | null = null;
  private accessTokenExpiresAt: string | null = null;

  constructor(
    private readonly userAuthApi: UserAuthApi,
    private readonly sessionStorage: SessionStorageService,
  ) {}

  getAuthSnapshot(): IAuthSnapshot {
    const cachedSession = this.sessionStorage.getCachedSession();
    return {
      username: cachedSession?.username ?? null,
      sessionToken: cachedSession?.sessionToken ?? null,
      accessToken: this.accessToken,
      accessTokenExpiresAt: this.accessTokenExpiresAt,
    };
  }

  clearAuthState(): void {
    this.sessionStorage.clearCachedSession();
    this.accessToken = null;
    this.accessTokenExpiresAt = null;
  }

  login(username: string, password: string): Observable<void> {
    const request: LoginUserRequestDto = { username, password };
    return this.userAuthApi.loginUser(request).pipe(
      map((response) => {
        this.sessionStorage.setCachedSession({
          username: response.data.username,
          sessionToken: response.data.sessionToken,
          sessionExpiresAt: response.data.sessionExpiresAt,
        });

        this.accessToken = response.data.accessToken;
        this.accessTokenExpiresAt = response.data.AccessTokenExpiresAt;
      }),
    );
  }

  register(username: string, password: string): Observable<void> {
    const request: RegisterUserRequestDto = { username, password };
    return this.userAuthApi.registerUser(request).pipe(map(() => undefined));
  }

  logout(): Observable<void> {
    const cachedSession = this.sessionStorage.getCachedSession();
    if (cachedSession == null) {
      this.clearAuthState();
      return of(undefined);
    }

    const request: LogoutUserRequestDto = {
      username: cachedSession.username,
      sessionToken: cachedSession.sessionToken,
    };

    return this.userAuthApi.logoutUser(request).pipe(
      map(() => undefined),
      catchError(() => of(undefined)),
      map(() => {
        this.clearAuthState();
      }),
    );
  }

  getValidAccessToken(): Observable<string> {
    const cachedSession = this.sessionStorage.getCachedSession();
    if (cachedSession == null) {
      return throwError(() => new Error('No cached session.'));
    }

    if (this.isIsoInstantInPast(cachedSession.sessionExpiresAt)) {
      this.clearAuthState();
      return throwError(() => new Error('Cached session is expired.'));
    }

    if (this.accessToken != null && !this.isIsoInstantInPast(this.accessTokenExpiresAt)) {
      return of(this.accessToken);
    }

    return this.refreshAccessToken().pipe(
      switchMap((token) => {
        if (token == null) {
          this.clearAuthState();
          return throwError(() => new Error('Session refresh did not return a valid access token.'));
        }
        return of(token);
      }),
    );
  }

  refreshAccessToken(): Observable<string | null> {
    const cachedSession = this.sessionStorage.getCachedSession();
    if (cachedSession == null) {
      return of(null);
    }

    if (this.isIsoInstantInPast(cachedSession.sessionExpiresAt)) {
      return of(null);
    }

    const request: RefreshLoginRequestDto = {
      username: cachedSession.username,
      sessionToken: cachedSession.sessionToken,
    };

    return this.userAuthApi.refreshLogin(request).pipe(
      map((response) => {
        const accessToken: string | null = response.data.accessToken;
        const accessTokenExpiresAt: string | null = response.data.AccessTokenExpiresAt;

        if (accessToken == null || accessTokenExpiresAt == null) {
          return null;
        }

        this.accessToken = accessToken;
        this.accessTokenExpiresAt = accessTokenExpiresAt;
        return accessToken;
      }),
      catchError(() => of(null)),
    );
  }

  private isIsoInstantInPast(value: string | null): boolean {
    if (value == null) {
      return false;
    }

    const parsedMs: number = Date.parse(value);
    if (Number.isNaN(parsedMs)) {
      return false;
    }

    return parsedMs <= Date.now();
  }
}
