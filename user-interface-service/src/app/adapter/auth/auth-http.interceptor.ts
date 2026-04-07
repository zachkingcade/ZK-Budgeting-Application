import { HttpErrorResponse, HttpEvent, HttpHandlerFn, HttpInterceptorFn, HttpRequest } from '@angular/common/http';
import { inject } from '@angular/core';
import { Router } from '@angular/router';
import { Observable, catchError, switchMap, throwError } from 'rxjs';
import { AuthManagerService } from '../../application/auth/auth-manager.service';

const USER_SERVICE_BASE_URL: string = 'http://localhost:8082';

function isUserServiceRequest(request: HttpRequest<unknown>): boolean {
  return request.url.startsWith(USER_SERVICE_BASE_URL) && request.url.includes('/user/');
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

  if (isUserServiceRequest(initialRequest)) {
    return next(initialRequest);
  }

  // #region agent log
  fetch('http://127.0.0.1:7725/ingest/2fb30966-6fce-4ce3-9190-7064cc5feee2',{method:'POST',headers:{'Content-Type':'application/json','X-Debug-Session-Id':'4000cb'},body:JSON.stringify({sessionId:'4000cb',runId:'pre-fix',hypothesisId:'H4',location:'auth-http.interceptor.ts:60',message:'Intercept request',data:{url:initialRequest.url,method:initialRequest.method,retry:getRetryCount(initialRequest)},timestamp:Date.now()})}).catch(()=>{});
  // #endregion

  return authManager.getValidAccessToken().pipe(
    catchError((error) => {
      // #region agent log
      fetch('http://127.0.0.1:7725/ingest/2fb30966-6fce-4ce3-9190-7064cc5feee2',{method:'POST',headers:{'Content-Type':'application/json','X-Debug-Session-Id':'4000cb'},body:JSON.stringify({sessionId:'4000cb',runId:'pre-fix',hypothesisId:'H4',location:'auth-http.interceptor.ts:71',message:'Preflight token failed; redirect to login',data:{url:initialRequest.url},timestamp:Date.now()})}).catch(()=>{});
      // #endregion
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
        // #region agent log
        fetch('http://127.0.0.1:7725/ingest/2fb30966-6fce-4ce3-9190-7064cc5feee2',{method:'POST',headers:{'Content-Type':'application/json','X-Debug-Session-Id':'4000cb'},body:JSON.stringify({sessionId:'4000cb',runId:'pre-fix',hypothesisId:'H4',location:'auth-http.interceptor.ts:95',message:'401 after max retries; redirect to login',data:{url:initialRequest.url,retry:nextRetryCount},timestamp:Date.now()})}).catch(()=>{});
        // #endregion
        redirectToLoginAndClearAuthState(authManager, router);
        return throwError(() => error);
      }

      // #region agent log
      fetch('http://127.0.0.1:7725/ingest/2fb30966-6fce-4ce3-9190-7064cc5feee2',{method:'POST',headers:{'Content-Type':'application/json','X-Debug-Session-Id':'4000cb'},body:JSON.stringify({sessionId:'4000cb',runId:'pre-fix',hypothesisId:'H4',location:'auth-http.interceptor.ts:104',message:'401 received; attempting refresh',data:{url:initialRequest.url,retry:nextRetryCount},timestamp:Date.now()})}).catch(()=>{});
      // #endregion

      return authManager.refreshAccessToken().pipe(
        switchMap((refreshedToken) => {
          if (refreshedToken == null) {
            // #region agent log
            fetch('http://127.0.0.1:7725/ingest/2fb30966-6fce-4ce3-9190-7064cc5feee2',{method:'POST',headers:{'Content-Type':'application/json','X-Debug-Session-Id':'4000cb'},body:JSON.stringify({sessionId:'4000cb',runId:'pre-fix',hypothesisId:'H4',location:'auth-http.interceptor.ts:112',message:'Refresh returned null; redirect to login',data:{url:initialRequest.url,retry:nextRetryCount},timestamp:Date.now()})}).catch(()=>{});
            // #endregion
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
          // #region agent log
          fetch('http://127.0.0.1:7725/ingest/2fb30966-6fce-4ce3-9190-7064cc5feee2',{method:'POST',headers:{'Content-Type':'application/json','X-Debug-Session-Id':'4000cb'},body:JSON.stringify({sessionId:'4000cb',runId:'pre-fix',hypothesisId:'H4',location:'auth-http.interceptor.ts:125',message:'Refresh threw; redirect to login',data:{url:initialRequest.url,retry:nextRetryCount},timestamp:Date.now()})}).catch(()=>{});
          // #endregion
          redirectToLoginAndClearAuthState(authManager, router);
          return throwError(() => error);
        }),
      );
    }),
  );
};
