import { Injectable, signal } from '@angular/core';
import {
  DEFAULT_UI_THEME,
  UI_THEME_LOCAL_STORAGE_KEY,
  UiThemeId,
  isUiThemeId,
  resolveUiThemeId,
} from '../../domain/ui-preferences/ui-theme-settings';

@Injectable({
  providedIn: 'root',
})
export class ThemeService {
  readonly currentTheme = signal<UiThemeId>(DEFAULT_UI_THEME);

  initFromStorage(): void {
    const stored = localStorage.getItem(UI_THEME_LOCAL_STORAGE_KEY);
    const theme = stored && isUiThemeId(stored) ? stored : DEFAULT_UI_THEME;
    this.applyTheme(theme, { persist: false });
  }

  applyTheme(themeId: UiThemeId, options?: { persist?: boolean }): void {
    const resolved = resolveUiThemeId(themeId);
    document.documentElement.dataset['theme'] = resolved;
    const meta = resolved.endsWith('-dark');
    document.body.style.colorScheme = meta ? 'dark' : 'light';
    this.currentTheme.set(resolved);
    if (options?.persist !== false) {
      localStorage.setItem(UI_THEME_LOCAL_STORAGE_KEY, resolved);
    }
  }
}

export function initThemeFromStorage(): () => void {
  const stored = localStorage.getItem(UI_THEME_LOCAL_STORAGE_KEY);
  const theme = stored && isUiThemeId(stored) ? stored : DEFAULT_UI_THEME;
  document.documentElement.dataset['theme'] = theme;
  document.body.style.colorScheme = theme.endsWith('-dark') ? 'dark' : 'light';
  return () => undefined;
}
