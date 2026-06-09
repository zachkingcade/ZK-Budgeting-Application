import { TestBed } from '@angular/core/testing';
import { provideZonelessChangeDetection } from '@angular/core';

import { WelcomeOnboardingService } from './welcome-onboarding.service';

describe('WelcomeOnboardingService', () => {
  let service: WelcomeOnboardingService;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [provideZonelessChangeDetection(), WelcomeOnboardingService],
    });
    service = TestBed.inject(WelcomeOnboardingService);
  });

  it('should consume scheduled welcome only once', () => {
    service.scheduleWelcomeAfterLogin();
    expect(service.consumeScheduledWelcome()).toBe(true);
    expect(service.consumeScheduledWelcome()).toBe(false);
  });

  it('should clear scheduled welcome', () => {
    service.scheduleWelcomeAfterLogin();
    service.clearScheduledWelcome();
    expect(service.consumeScheduledWelcome()).toBe(false);
  });
});
