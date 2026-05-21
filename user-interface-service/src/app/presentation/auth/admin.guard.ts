import { CanMatchFn, Router, UrlTree } from '@angular/router';
import { inject } from '@angular/core';
import { Observable, catchError, map, of, switchMap } from 'rxjs';
import { AuthManagerService } from '../../application/auth/auth-manager.service';

export const adminGuard: CanMatchFn = (): Observable<boolean | UrlTree> => {
  const authManager: AuthManagerService = inject(AuthManagerService);
  const router: Router = inject(Router);

  return authManager.getValidAccessToken().pipe(
    switchMap(() => {
      if (authManager.isAdmin()) {
        return of(true);
      }
      return of(router.parseUrl('/ledger'));
    }),
    catchError(() => of(router.parseUrl('/login'))),
  );
};
