import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideZonelessChangeDetection } from '@angular/core';
import { ActivatedRoute, convertToParamMap, provideRouter, Router } from '@angular/router';
import { of, throwError } from 'rxjs';

import { RegisterPageComponent } from './register-page.component';
import { AuthManagerService } from '../../../application/auth/auth-manager.service';

describe('RegisterPageComponent', () => {
  let component: RegisterPageComponent;
  let fixture: ComponentFixture<RegisterPageComponent>;

  const authManagerMock: Pick<AuthManagerService, 'register'> = {
    register: jasmine.createSpy('register').and.returnValue(of(void 0)),
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
    (authManagerMock.register as jasmine.Spy).and.returnValue(of(void 0));
    (authManagerMock.register as jasmine.Spy).calls.reset();

    await TestBed.configureTestingModule({
      imports: [RegisterPageComponent],
      providers: [
        provideZonelessChangeDetection(),
        provideRouter([]),
        { provide: AuthManagerService, useValue: authManagerMock },
        { provide: ActivatedRoute, useValue: activatedRouteStub },
      ],
    }).compileComponents();

    router = TestBed.inject(Router);
    spyOn(router, 'navigateByUrl').and.resolveTo(true);

    fixture = TestBed.createComponent(RegisterPageComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should register and navigate when credentials meet requirements', async () => {
    // HAPPY PATH
    component.form.controls.username.setValue('alice123');
    component.form.controls.password.setValue('Password123!');

    component.submit();

    expect((authManagerMock.register as jasmine.Spy)).toHaveBeenCalledWith('alice123', 'Password123!');
    await fixture.whenStable();
    expect(router.navigateByUrl).toHaveBeenCalledWith('/login');
  });

  it('should show requirements message when invalid username/password', () => {
    /*
    NEGATIVE PATH: method=submit,
    input={username: short or contains @, password: missing requirements},
    expected failure message=Please meet all username and password requirements.
    */
    component.form.controls.username.setValue('a@b');
    component.form.controls.password.setValue('short');

    component.submit();

    expect((authManagerMock.register as jasmine.Spy)).not.toHaveBeenCalled();
    expect(component.errorMessage()).toBe('Please meet all username and password requirements.');
  });

  it('should show error message when register fails', () => {
    /*
    NEGATIVE PATH: method=register,
    input={username: alice123, password: Password123!},
    expected failure message=Registration failed. Please try a different username.
    */
    (authManagerMock.register as jasmine.Spy).and.returnValue(throwError(() => new Error('duplicate')));

    component.form.controls.username.setValue('alice123');
    component.form.controls.password.setValue('Password123!');

    component.submit();

    expect(component.errorMessage()).toBe('Registration failed. Please try a different username.');
    expect(component.submitting()).toBeFalse();
  });
});

