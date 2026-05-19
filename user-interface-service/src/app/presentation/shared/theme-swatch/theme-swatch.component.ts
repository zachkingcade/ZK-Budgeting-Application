import { Component, input, output } from '@angular/core';
import { UiThemeId, UI_THEME_META } from '../../../domain/ui-preferences/ui-theme-settings';

@Component({
  selector: 'app-theme-swatch',
  standalone: true,
  template: `
    <button
      type="button"
      class="themeSwatch"
      [class.themeSwatch--selected]="selected()"
      [attr.aria-label]="label()"
      [attr.aria-pressed]="selected()"
      (click)="selectedChange.emit(themeId())"
    >
      <span
        class="themeSwatch__gradient"
        [style.background]="gradientStyle()"
      ></span>
      <span class="themeSwatch__label">{{ label() }}</span>
    </button>
  `,
  styles: [
    `
      .themeSwatch {
        display: flex;
        flex-direction: column;
        align-items: center;
        gap: 0.35rem;
        padding: 0.5rem;
        border: 2px solid transparent;
        border-radius: 8px;
        background: transparent;
        cursor: pointer;
        min-width: 5.5rem;
      }
      .themeSwatch--selected {
        border-color: var(--mat-sys-primary, #2563eb);
      }
      .themeSwatch__gradient {
        width: 2.75rem;
        height: 2.75rem;
        border-radius: 50%;
        border: 1px solid var(--app-border, rgba(0, 0, 0, 0.15));
      }
      .themeSwatch__label {
        font-size: 0.7rem;
        text-align: center;
        color: var(--mat-sys-on-surface);
        line-height: 1.2;
      }
    `,
  ],
})
export class ThemeSwatchComponent {
  readonly themeId = input.required<UiThemeId>();
  readonly selected = input(false);
  readonly selectedChange = output<UiThemeId>();

  protected label(): string {
    return UI_THEME_META.find((m) => m.id === this.themeId())?.label ?? this.themeId();
  }

  protected gradientStyle(): string {
    const meta = UI_THEME_META.find((m) => m.id === this.themeId());
    const accent = meta?.accentColor ?? '#2563eb';
    const fadeTo = meta?.mode === 'dark' ? '#000000' : '#ffffff';
    return `linear-gradient(135deg, ${accent} 0%, ${fadeTo} 100%)`;
  }
}
