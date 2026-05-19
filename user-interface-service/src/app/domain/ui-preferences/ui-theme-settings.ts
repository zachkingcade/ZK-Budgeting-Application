export type UiThemeId =
  | 'light-blue'
  | 'dark-blue'
  | 'orange'
  | 'purple'
  | 'light-blue-dark'
  | 'dark-blue-dark'
  | 'orange-dark'
  | 'purple-dark';

export const UI_THEME_SETTING_KEY = 'ui.theme';

export const DEFAULT_UI_THEME: UiThemeId = 'light-blue';

export const UI_THEME_LOCAL_STORAGE_KEY = 'zk.ui.theme';

export const UI_THEME_IDS: readonly UiThemeId[] = [
  'light-blue',
  'dark-blue',
  'orange',
  'purple',
  'light-blue-dark',
  'dark-blue-dark',
  'orange-dark',
  'purple-dark',
] as const;

export const UI_LIGHT_THEME_IDS: readonly UiThemeId[] = ['light-blue', 'dark-blue', 'orange', 'purple'] as const;

export const UI_DARK_THEME_IDS: readonly UiThemeId[] = [
  'light-blue-dark',
  'dark-blue-dark',
  'orange-dark',
  'purple-dark',
] as const;

export interface UiThemeMeta {
  id: UiThemeId;
  label: string;
  /** Base accent for gradient swatch. */
  accentColor: string;
  mode: 'light' | 'dark';
}

export const UI_THEME_META: readonly UiThemeMeta[] = [
  { id: 'light-blue', label: 'Light blue', accentColor: '#2563eb', mode: 'light' },
  { id: 'dark-blue', label: 'Dark blue', accentColor: '#1e3a8a', mode: 'light' },
  { id: 'orange', label: 'Orange', accentColor: '#ea580c', mode: 'light' },
  { id: 'purple', label: 'Purple', accentColor: '#7c3aed', mode: 'light' },
  { id: 'light-blue-dark', label: 'Light blue (dark)', accentColor: '#60a5fa', mode: 'dark' },
  { id: 'dark-blue-dark', label: 'Dark blue (dark)', accentColor: '#3b82f6', mode: 'dark' },
  { id: 'orange-dark', label: 'Orange (dark)', accentColor: '#fb923c', mode: 'dark' },
  { id: 'purple-dark', label: 'Purple (dark)', accentColor: '#a78bfa', mode: 'dark' },
] as const;

export function isUiThemeId(value: string): value is UiThemeId {
  return (UI_THEME_IDS as readonly string[]).includes(value);
}

export function resolveUiThemeId(raw: string | undefined): UiThemeId {
  if (raw && isUiThemeId(raw)) {
    return raw;
  }
  return DEFAULT_UI_THEME;
}
