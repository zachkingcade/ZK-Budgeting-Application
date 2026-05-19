import { Component, DestroyRef, OnInit, signal } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatExpansionModule } from '@angular/material/expansion';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatOptionModule } from '@angular/material/core';
import { MatSelectModule } from '@angular/material/select';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { PageCage } from '../../page-cage/page-cage.component';
import { SettingsApplicationService } from '../../../application/settings/settings.application-service';
import { UserPreferencesService } from '../../../application/settings/user-preferences.service';
import { ThemeService } from '../../../application/theme/theme.service';
import {
  AccountingPeriodFrequencyUnit,
  maxFrequencyCount,
} from '../../../domain/accounting-period/accounting-period-settings';
import { ACCOUNT_TYPES_SORT_BY_OPTIONS } from '../../../domain/ui-preferences/account-types-display-settings';
import { ACCOUNTS_SORT_BY_OPTIONS } from '../../../domain/ui-preferences/accounts-display-settings';
import {
  LEDGER_DATE_RANGE_OPTIONS,
  LEDGER_SORT_BY_OPTIONS,
} from '../../../domain/ui-preferences/ledger-display-settings';
import { EMPTY_UI_PREFERENCES, UiPreferencesForm } from '../../../domain/ui-preferences/ui-preferences';
import { UI_DARK_THEME_IDS, UI_LIGHT_THEME_IDS, UiThemeId } from '../../../domain/ui-preferences/ui-theme-settings';
import { ThemeSwatchComponent } from '../../shared/theme-swatch/theme-swatch.component';
import { AccountTypesApplicationService } from '../../../application/ledger/account-types.application-service';
import { ContextHelpButtonComponent } from '../../../help/components/context-help-button/context-help-button.component';
@Component({
  selector: 'app-settings-page',
  standalone: true,
  imports: [
    ContextHelpButtonComponent,
    PageCage,
    ReactiveFormsModule,
    MatExpansionModule,
    MatFormFieldModule,
    MatSelectModule,
    MatOptionModule,
    MatInputModule,
    MatButtonModule,
    MatSnackBarModule,
    ThemeSwatchComponent,
  ],
  templateUrl: './settings-page.component.html',
  styleUrl: './settings-page.component.scss',
})
export class SettingsPageComponent implements OnInit {
  protected readonly loading = signal(false);
  protected readonly saving = signal(false);
  protected readonly loadError = signal<string | null>(null);

  protected readonly ledgerDateOptions = LEDGER_DATE_RANGE_OPTIONS;
  protected readonly ledgerSortOptions = LEDGER_SORT_BY_OPTIONS;
  protected readonly accountsSortOptions = ACCOUNTS_SORT_BY_OPTIONS;
  protected readonly accountTypesSortOptions = ACCOUNT_TYPES_SORT_BY_OPTIONS;
  protected readonly lightThemes = UI_LIGHT_THEME_IDS;
  protected readonly darkThemes = UI_DARK_THEME_IDS;

  protected readonly accountTypeOptions = signal<{ id: number; label: string }[]>([]);

  private savedSnapshot: UiPreferencesForm = { ...EMPTY_UI_PREFERENCES };

  protected readonly form;

  constructor(
    private readonly fb: FormBuilder,
    private readonly settingsService: SettingsApplicationService,
    private readonly userPreferences: UserPreferencesService,
    private readonly themeService: ThemeService,
    private readonly accountTypesService: AccountTypesApplicationService,
    private readonly snackBar: MatSnackBar,
    private readonly destroyRef: DestroyRef,
  ) {
    this.form = this.fb.group({
      frequencyUnit: this.fb.nonNullable.control<AccountingPeriodFrequencyUnit | ''>(''),
      frequencyCount: this.fb.control<number | null>(null, [Validators.required, Validators.min(1)]),
      firstStartDate: this.fb.nonNullable.control('', Validators.required),
      ledgerDateRange: this.fb.nonNullable.control<string>(''),
      ledgerSortBy: this.fb.nonNullable.control<string>(''),
      accountsAccountTypeIds: this.fb.nonNullable.control<number[]>([]),
      accountsSortBy: this.fb.nonNullable.control<string>(''),
      accountTypesSortBy: this.fb.nonNullable.control<string>(''),
      theme: this.fb.nonNullable.control<UiThemeId>('light-blue'),
    });
  }

  ngOnInit(): void {
    this.accountTypesService
      .getAll()
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: (res) => {
          const list = res.data?.accountTypeList ?? [];
          this.accountTypeOptions.set(
            list.map((t) => ({ id: t.id, label: t.description })),
          );
        },
      });
    this.loadSettings();
  }

  protected maxCount(): number {
    const unit = this.form.controls.frequencyUnit.value;
    if (!unit) {
      return 365;
    }
    return maxFrequencyCount(unit);
  }

  protected selectTheme(themeId: UiThemeId): void {
    this.form.controls.theme.setValue(themeId);
    this.themeService.applyTheme(themeId);
  }

  protected isThemeSelected(themeId: UiThemeId): boolean {
    return this.form.controls.theme.value === themeId;
  }

  protected save(): void {
    const unit = this.form.controls.frequencyUnit.value;
    const hasAccounting =
      unit !== '' ||
      this.form.controls.frequencyCount.value != null ||
      this.form.controls.firstStartDate.value !== '';

    if (hasAccounting) {
      if (this.form.controls.frequencyCount.invalid || this.form.controls.firstStartDate.invalid) {
        this.form.markAllAsTouched();
        return;
      }
      if (!unit) {
        this.snackBar.open('Select a frequency unit', 'Dismiss', { duration: 4000 });
        return;
      }
      const count = this.form.controls.frequencyCount.value;
      if (count == null || count > maxFrequencyCount(unit)) {
        this.snackBar.open('Frequency count is out of range', 'Dismiss', { duration: 4000 });
        return;
      }
    }

    const payload = this.formToPreferences();
    this.saving.set(true);
    this.settingsService
      .saveUiPreferences(payload)
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: () => {
          this.savedSnapshot = payload;
          this.userPreferences.refresh().pipe(takeUntilDestroyed(this.destroyRef)).subscribe();
          this.themeService.applyTheme(payload.theme);
          this.saving.set(false);
          this.snackBar.open('Settings saved', 'Dismiss', { duration: 3000 });
        },
        error: (err: Error) => {
          this.saving.set(false);
          this.snackBar.open(err.message ?? 'Failed to save settings', 'Dismiss', { duration: 5000 });
        },
      });
  }

  protected clear(): void {
    this.applyForm(this.savedSnapshot);
    this.themeService.applyTheme(this.savedSnapshot.theme);
  }

  private loadSettings(): void {
    this.loading.set(true);
    this.settingsService
      .loadUiPreferences()
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: (settings) => {
          this.savedSnapshot = settings;
          this.applyForm(settings);
          this.loading.set(false);
        },
        error: () => {
          this.loadError.set('Failed to load settings');
          this.loading.set(false);
        },
      });
  }

  private formToPreferences(): UiPreferencesForm {
    return {
      accountingPeriod: {
        frequencyUnit: this.form.controls.frequencyUnit.value,
        frequencyCount: this.form.controls.frequencyCount.value,
        firstStartDate: this.form.controls.firstStartDate.value,
      },
      ledger: {
        dateRange: this.form.controls.ledgerDateRange.value as UiPreferencesForm['ledger']['dateRange'],
        sortBy: this.form.controls.ledgerSortBy.value as UiPreferencesForm['ledger']['sortBy'],
      },
      accounts: {
        accountTypeIds: [...this.form.controls.accountsAccountTypeIds.value],
        sortBy: this.form.controls.accountsSortBy.value as UiPreferencesForm['accounts']['sortBy'],
      },
      accountTypes: {
        sortBy: this.form.controls.accountTypesSortBy.value as UiPreferencesForm['accountTypes']['sortBy'],
      },
      theme: this.form.controls.theme.value,
    };
  }

  private applyForm(settings: UiPreferencesForm): void {
    this.form.setValue({
      frequencyUnit: settings.accountingPeriod.frequencyUnit,
      frequencyCount: settings.accountingPeriod.frequencyCount,
      firstStartDate: settings.accountingPeriod.firstStartDate,
      ledgerDateRange: settings.ledger.dateRange,
      ledgerSortBy: settings.ledger.sortBy,
      accountsAccountTypeIds: [...settings.accounts.accountTypeIds],
      accountsSortBy: settings.accounts.sortBy,
      accountTypesSortBy: settings.accountTypes.sortBy,
      theme: settings.theme,
    });
  }
}
