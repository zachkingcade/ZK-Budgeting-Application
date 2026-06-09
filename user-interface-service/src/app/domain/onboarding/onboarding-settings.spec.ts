import { isWelcomeDismissed } from './onboarding-settings';

describe('onboarding-settings', () => {
  it('should treat true string as dismissed', () => {
    expect(isWelcomeDismissed('true')).toBe(true);
  });

  it('should treat missing or other values as not dismissed', () => {
    expect(isWelcomeDismissed(undefined)).toBe(false);
    expect(isWelcomeDismissed('')).toBe(false);
    expect(isWelcomeDismissed('false')).toBe(false);
  });
});
