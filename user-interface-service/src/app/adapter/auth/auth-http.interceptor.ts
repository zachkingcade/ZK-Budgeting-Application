import { HttpErrorResponse, HttpEvent, HttpHandlerFn, HttpInterceptorFn, HttpRequest } from '@angular/common/http';
import { inject } from '@angular/core';
import { Router } from '@angular/router';
import { Observable, catchError, switchMap, throwError } from 'rxjs';
import { AuthManagerService } from '../../application/auth/auth-manager.service';
import { environment } from '../../../environments/environment';

const USER_SERVICE_BASE_URL: string = environment.userApiUrl;

const USER_SERVICE_PUBLIC_PATHS = [
  '/user/register',
  '/user/login',
  '/user/logout',
  '/user/refresh',
  '/user/service/login',
] as const;

function isUserServicePublicRequest(request: HttpRequest<unknown>): boolean {
  if (!request.url.startsWith(USER_SERVICE_BASE_URL)) {
    return false;
  }
  const path = request.url.substring(USER_SERVICE_BASE_URL.length).split('?')[0];
  return USER_SERVICE_PUBLIC_PATHS.some((publicPath) => path === publicPath);
}

function getRetryCount(request: HttpRequest<unknown>): number {
  const headerValue: string | null = request.headers.get('X-Auth-Retry');
  if (headerValue == null) {
    return 0;
  }

  const parsed: number = Number.parseInt(headerValue, 10);
  if (Number.isNaN(parsed)) {
    return 0;
  }

  return parsed;
}

function withRetryCount(request: HttpRequest<unknown>, retryCount: number): HttpRequest<unknown> {
  return request.clone({
    headers: request.headers.set('X-Auth-Retry', String(retryCount)),
  });
}

function withBearerToken(request: HttpRequest<unknown>, accessToken: string): HttpRequest<unknown> {
  return request.clone({
    setHeaders: {
      Authorization: `Bearer ${accessToken}`,
    },
  });
}

function redirectToLoginAndClearAuthState(authManager: AuthManagerService, router: Router): void {
  authManager.clearAuthState();
  void router.navigateByUrl('/login');
}

export const authHttpInterceptor: HttpInterceptorFn = (
  initialRequest: HttpRequest<unknown>,
  next: HttpHandlerFn,
): Observable<HttpEvent<unknown>> => {
  const authManager: AuthManagerService = inject(AuthManagerService);
  const router: Router = inject(Router);

  if (isUserServicePublicRequest(initialRequest)) {
    return next(initialRequest);
  }

  return authManager.getValidAccessToken().pipe(
    catchError((error) => {
      redirectToLoginAndClearAuthState(authManager, router);
      return throwError(() => error);
    }),
    switchMap((token) => next(withBearerToken(initialRequest, token))),
    catchError((error) => {
      if (!(error instanceof HttpErrorResponse)) {
        return throwError(() => error);
      }

      if (error.status !== 401) {
        return throwError(() => error);
      }

      const currentRetryCount: number = getRetryCount(initialRequest);
      const nextRetryCount: number = currentRetryCount + 1;
      if (nextRetryCount > 3) {
        redirectToLoginAndClearAuthState(authManager, router);
        return throwError(() => error);
      }

      return authManager.refreshAccessToken().pipe(
        switchMap((refreshedToken) => {
          if (refreshedToken == null) {
            redirectToLoginAndClearAuthState(authManager, router);
            return throwError(() => error);
          }

          const retried: HttpRequest<unknown> = withRetryCount(
            withBearerToken(initialRequest, refreshedToken),
            nextRetryCount,
          );
          return next(retried);
        }),
        catchError(() => {
          redirectToLoginAndClearAuthState(authManager, router);
          return throwError(() => error);
        }),
      );
    }),
  );
};
