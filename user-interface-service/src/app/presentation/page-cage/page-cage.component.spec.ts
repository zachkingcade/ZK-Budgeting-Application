import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideZonelessChangeDetection } from '@angular/core';
import { ActivatedRoute, convertToParamMap, provideRouter } from '@angular/router';
import { MatDialog } from '@angular/material/dialog';
import { of } from 'rxjs';

import { AuthManagerService } from '../../application/auth/auth-manager.service';
import { WelcomeOnboardingService } from '../../application/onboarding/welcome-onboarding.service';
import { SettingsApplicationService } from '../../application/settings/settings.application-service';
import { UserPreferencesService } from '../../application/settings/user-preferences.service';
import { EMPTY_UI_PREFERENCES } from '../../domain/ui-preferences/ui-preferences';
import { PageCage } from './page-cage.component';
import { WelcomeDialogComponent } from '../shared/welcome-dialog/welcome-dialog.component';

describe('PageCage', () => {
  let component: PageCage;
  let fixture: ComponentFixture<PageCage>;
  let dialog: MatDialog;

  const authMock: Pick<AuthManagerService, 'getAuthSnapshot' | 'logout'> = {
    getAuthSnapshot: () => ({
      username: null,
      sessionToken: null,
      accessToken: null,
      accessTokenExpiresAt: null,
      roles: [],
    }),
    logout: () => of(undefined),
  };

  const userPreferencesMock: Pick<UserPreferencesService, 'ensureLoaded' | 'isWelcomeDismissed' | 'refresh'> = {
    ensureLoaded: jasmine.createSpy('ensureLoaded').and.returnValue(of(EMPTY_UI_PREFERENCES)),
    isWelcomeDismissed: jasmine.createSpy('isWelcomeDismissed').and.returnValue(false),
    refresh: jasmine.createSpy('refresh').and.returnValue(of(EMPTY_UI_PREFERENCES)),
  };

  const settingsServiceMock: Pick<SettingsApplicationService, 'saveWelcomeDismissed'> = {
    saveWelcomeDismissed: jasmine.createSpy('saveWelcomeDismissed').and.returnValue(of(undefined)),
  };

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [PageCage],
      providers: [
        provideZonelessChangeDetection(),
        provideRouter([]),
        { provide: AuthManagerService, useValue: authMock },
        { provide: UserPreferencesService, useValue: userPreferencesMock },
        { provide: SettingsApplicationService, useValue: settingsServiceMock },
        {
          provide: ActivatedRoute,
          useValue: {
            snapshot: {
              paramMap: convertToParamMap({}),
              queryParamMap: convertToParamMap({}),
            },
            params: of({}),
            queryParams: of({}),
            fragment: of(null),
            data: of({}),
            url: of([]),
          },
        },
      ],
    }).compileComponents();

    dialog = TestBed.inject(MatDialog);
    spyOn(dialog, 'open').and.returnValue({
      afterClosed: () => of(undefined),
    } as any);

    fixture = TestBed.createComponent(PageCage);
    component = fixture.componentInstance;
  });

  it('should create', () => {
    fixture.detectChanges();
    expect(component).toBeTruthy();
  });

  it('should open welcome dialog when scheduled and not dismissed', () => {
    TestBed.inject(WelcomeOnboardingService).scheduleWelcomeAfterLogin();
    (userPreferencesMock.isWelcomeDismissed as jasmine.Spy).and.returnValue(false);

    fixture.detectChanges();

    expect(dialog.open).toHaveBeenCalledWith(WelcomeDialogComponent, jasmine.any(Object));
  });

  it('should skip welcome dialog when dismissed in settings', () => {
    TestBed.inject(WelcomeOnboardingService).scheduleWelcomeAfterLogin();
    (userPreferencesMock.isWelcomeDismissed as jasmine.Spy).and.returnValue(true);

    fixture.detectChanges();

    expect(dialog.open).not.toHaveBeenCalled();
  });
});
