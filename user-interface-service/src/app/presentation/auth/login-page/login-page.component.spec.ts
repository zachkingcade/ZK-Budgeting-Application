import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideZonelessChangeDetection } from '@angular/core';
import { ActivatedRoute, convertToParamMap, provideRouter, Router } from '@angular/router';
import { of, throwError } from 'rxjs';

import { LoginPageComponent } from './login-page.component';
import { AuthManagerService } from '../../../application/auth/auth-manager.service';

describe('LoginPageComponent', () => {
  let component: LoginPageComponent;
  let fixture: ComponentFixture<LoginPageComponent>;

  const authManagerMock: Pick<AuthManagerService, 'login'> = {
    login: jasmine.createSpy('login').and.returnValue(of(void 0)),
  } as any;

  let router: Router;

  const activatedRouteStub: Partial<ActivatedRoute> = {
    snapshot: {
      paramMap: convertToParamMap({}),
      queryParamMap: convertToParamMap({}),
    } as any,
    params: of({}),
    queryParams: of({}),
    fragment: of(null),
    data: of({}),
    url: of([] as any),
  };

  beforeEach(async () => {
    (authManagerMock.login as jasmine.Spy).and.returnValue(of(void 0));
    (authManagerMock.login as jasmine.Spy).calls.reset();

    await TestBed.configureTestingModule({
      imports: [LoginPageComponent],
      providers: [
        provideZonelessChangeDetection(),
        provideRouter([]),
        { provide: AuthManagerService, useValue: authManagerMock },
        { provide: ActivatedRoute, useValue: activatedRouteStub },
      ],
    }).compileComponents();

    router = TestBed.inject(Router);
    spyOn(router, 'navigateByUrl').and.resolveTo(true);

    fixture = TestBed.createComponent(LoginPageComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should login and navigate when credentials valid', async () => {
    // HAPPY PATH
    component.form.controls.username.setValue('alice');
    component.form.controls.password.setValue('Password123!');

    component.submit();

    expect((authManagerMock.login as jasmine.Spy)).toHaveBeenCalledWith('alice', 'Password123!');
    await fixture.whenStable();
    expect(router.navigateByUrl).toHaveBeenCalledWith('/ledger');
    expect(component.errorMessage()).toBeNull();
  });

  it('should show validation message when username or password blank', () => {
    /*
    NEGATIVE PATH: method=submit,
    input={username: blank, password: blank},
    expected failure message=Username and password are required.
    */
    component.form.controls.username.setValue(' ');
    component.form.controls.password.setValue(' ');

    component.submit();

    expect((authManagerMock.login as jasmine.Spy)).not.toHaveBeenCalled();
    expect(component.errorMessage()).toBe('Username and password are required.');
  });

  it('should show error message when login fails', () => {
    /*
    NEGATIVE PATH: method=login,
    input={username: alice, password: wrong},
    expected failure message=Login failed. Please check your credentials and try again.
    */
    (authManagerMock.login as jasmine.Spy).and.returnValue(throwError(() => new Error('bad credentials')));

    component.form.controls.username.setValue('alice');
    component.form.controls.password.setValue('wrong');

    component.submit();

    expect(component.errorMessage()).toBe('Login failed. Please check your credentials and try again.');
    expect(component.submitting()).toBeFalse();
  });
});

