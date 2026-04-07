import { CanMatchFn, Router, UrlTree } from '@angular/router';
import { inject } from '@angular/core';
import { Observable, catchError, map, of } from 'rxjs';
import { AuthManagerService } from '../../application/auth/auth-manager.service';

export const authGuard: CanMatchFn = (): Observable<boolean | UrlTree> => {
  const authManager: AuthManagerService = inject(AuthManagerService);
  const router: Router = inject(Router);

  return authManager.getValidAccessToken().pipe(
    map(() => true),
    catchError(() => of(router.parseUrl('/login'))),
  );
};

