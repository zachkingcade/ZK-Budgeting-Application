import { Injectable } from '@angular/core';

@Injectable({
  providedIn: 'root',
})
export class WelcomeOnboardingService {
  private scheduled = false;

  scheduleWelcomeAfterLogin(): void {
    this.scheduled = true;
  }

  consumeScheduledWelcome(): boolean {
    if (!this.scheduled) {
      return false;
    }
    this.scheduled = false;
    return true;
  }

  clearScheduledWelcome(): void {
    this.scheduled = false;
  }
}
