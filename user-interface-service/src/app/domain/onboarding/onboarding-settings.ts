export const ONBOARDING_SETTING_KEYS = {
  welcomeDismissed: 'onboarding.welcome_dismissed',
} as const;

export interface OnboardingSettingsForm {
  welcomeDismissed: boolean;
}

export const EMPTY_ONBOARDING_SETTINGS: OnboardingSettingsForm = {
  welcomeDismissed: false,
};

export function isWelcomeDismissed(value: string | undefined): boolean {
  return value === 'true';
}
