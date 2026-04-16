import { TestBed } from '@angular/core/testing';
import { provideZonelessChangeDetection } from '@angular/core';
import { Route, Router, UrlSegment, UrlTree } from '@angular/router';
import { of, throwError } from 'rxjs';

import { authGuard } from './auth.guard';
import { AuthManagerService } from '../../application/auth/auth-manager.service';

describe('authGuard', () => {
  const routerMock: Pick<Router, 'parseUrl'> = {
    parseUrl: jasmine.createSpy('parseUrl').and.returnValue({} as UrlTree),
  };

  it('should allow match when token is valid', (done) => {
    // HAPPY PATH
    const authManagerMock: Pick<AuthManagerService, 'getValidAccessToken'> = {
      getValidAccessToken: jasmine.createSpy('getValidAccessToken').and.returnValue(of('token')),
    } as any;

    TestBed.configureTestingModule({
      providers: [
        provideZonelessChangeDetection(),
        { provide: AuthManagerService, useValue: authManagerMock },
        { provide: Router, useValue: routerMock },
      ],
    });

    TestBed.runInInjectionContext(() => {
      const result = authGuard({} as Route, [] as UrlSegment[]);
      (result as any).subscribe((value: boolean | UrlTree) => {
        expect(value).toBeTrue();
        done();
      });
    });
  });

  it('should redirect to /login when token retrieval fails', (done) => {
    /*
    NEGATIVE PATH: method=getValidAccessToken,
    input=throws,
    expected failure message=redirect /login
    */
    const authManagerMock: Pick<AuthManagerService, 'getValidAccessToken'> = {
      getValidAccessToken: jasmine.createSpy('getValidAccessToken').and.returnValue(
        throwError(() => new Error('no token')),
      ),
    } as any;

    TestBed.configureTestingModule({
      providers: [
        provideZonelessChangeDetection(),
        { provide: AuthManagerService, useValue: authManagerMock },
        { provide: Router, useValue: routerMock },
      ],
    });

    TestBed.runInInjectionContext(() => {
      const result = authGuard({} as Route, [] as UrlSegment[]);
      (result as any).subscribe((value: boolean | UrlTree) => {
        expect(routerMock.parseUrl).toHaveBeenCalledWith('/login');
        expect(value).toEqual({} as UrlTree);
        done();
      });
    });
  });
});

