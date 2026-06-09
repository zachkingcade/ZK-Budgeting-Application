import { HttpErrorResponse } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable, catchError, forkJoin, map, of, switchMap, throwError } from 'rxjs';
import { UserSettingsApi } from '../../adapter/user-service/api/user-settings.api';
import { IUserSettingDto } from '../../adapter/user-service/dto/user-settings.dto';
import {
  ACCOUNTING_PERIOD_SETTING_KEYS,
  AccountingPeriodSettingsForm,
  EMPTY_ACCOUNTING_PERIOD_SETTINGS,
  AccountingPeriodFrequencyUnit,
} from '../../domain/accounting-period/accounting-period-settings';
import {
  ACCOUNT_TYPES_DISPLAY_SETTING_KEYS,
  AccountTypesDisplaySettingsForm,
  EMPTY_ACCOUNT_TYPES_DISPLAY_SETTINGS,
  isAccountTypesSortByOption,
} from '../../domain/ui-preferences/account-types-display-settings';
import {
  ACCOUNTS_DISPLAY_SETTING_KEYS,
  AccountsDisplaySettingsForm,
  EMPTY_ACCOUNTS_DISPLAY_SETTINGS,
  isAccountsSortByOption,
  parseAccountTypeIdsJson,
  serializeAccountTypeIds,
} from '../../domain/ui-preferences/accounts-display-settings';
import {
  EMPTY_LEDGER_DISPLAY_SETTINGS,
  LEDGER_DISPLAY_SETTING_KEYS,
  LedgerDisplaySettingsForm,
  isLedgerDateRangeOption,
  isLedgerSortByOption,
} from '../../domain/ui-preferences/ledger-display-settings';
import {
  EMPTY_UI_PREFERENCES,
  UiPreferencesForm,
} from '../../domain/ui-preferences/ui-preferences';
import {
  ONBOARDING_SETTING_KEYS,
  isWelcomeDismissed,
} from '../../domain/onboarding/onboarding-settings';
import {
  UI_THEME_SETTING_KEY,
  resolveUiThemeId,
} from '../../domain/ui-preferences/ui-theme-settings';

@Injectable({
  providedIn: 'root',
})
export class SettingsApplicationService {
  constructor(private readonly userSettingsApi: UserSettingsApi) {}

  loadUiPreferences(): Observable<UiPreferencesForm> {
    return this.userSettingsApi.listAll().pipe(map((response) => this.mapAllSettings(response.data ?? [])));
  }

  loadAccountingPeriodSettings(): Observable<AccountingPeriodSettingsForm> {
    return this.loadUiPreferences().pipe(map((prefs) => prefs.accountingPeriod));
  }

  saveUiPreferences(form: UiPreferencesForm): Observable<void> {
    const entries: { name: string; value: string }[] = [
      { name: ACCOUNTING_PERIOD_SETTING_KEYS.frequencyUnit, value: form.accountingPeriod.frequencyUnit },
      {
        name: ACCOUNTING_PERIOD_SETTING_KEYS.frequencyCount,
        value: String(form.accountingPeriod.frequencyCount ?? ''),
      },
      { name: ACCOUNTING_PERIOD_SETTING_KEYS.firstStartDate, value: form.accountingPeriod.firstStartDate },
      { name: LEDGER_DISPLAY_SETTING_KEYS.dateRange, value: form.ledger.dateRange },
      { name: LEDGER_DISPLAY_SETTING_KEYS.sortBy, value: form.ledger.sortBy },
      {
        name: ACCOUNTS_DISPLAY_SETTING_KEYS.accountTypeIds,
        value: serializeAccountTypeIds(form.accounts.accountTypeIds),
      },
      { name: ACCOUNTS_DISPLAY_SETTING_KEYS.sortBy, value: form.accounts.sortBy },
      { name: ACCOUNT_TYPES_DISPLAY_SETTING_KEYS.sortBy, value: form.accountTypes.sortBy },
      { name: UI_THEME_SETTING_KEY, value: form.theme },
    ];

    return this.userSettingsApi.listAll().pipe(
      switchMap((response) => {
        const existingNames = new Set((response.data ?? []).map((s) => s.settingName));
        const upserts = entries.map((entry) => this.persistEntry(entry, existingNames));
        return forkJoin(upserts);
      }),
      map(() => undefined),
    );
  }

  saveAccountingPeriodSettings(form: AccountingPeriodSettingsForm): Observable<void> {
    return this.loadUiPreferences().pipe(
      switchMap((prefs) =>
        this.saveUiPreferences({
          ...prefs,
          accountingPeriod: form,
        }),
      ),
    );
  }

  saveWelcomeDismissed(dismissed: boolean): Observable<void> {
    return this.userSettingsApi.listAll().pipe(
      switchMap((response) => {
        const existingNames = new Set((response.data ?? []).map((s) => s.settingName));
        const entry = {
          name: ONBOARDING_SETTING_KEYS.welcomeDismissed,
          value: dismissed ? 'true' : '',
        };
        return this.persistEntry(entry, existingNames);
      }),
      map(() => undefined),
    );
  }

  private persistEntry(
    entry: { name: string; value: string },
    existingNames: Set<string>,
  ): Observable<unknown> {
    const exists = existingNames.has(entry.name);

    if (entry.value === '') {
      if (!exists) {
        return of(null);
      }
      return this.userSettingsApi.deleteByName(entry.name).pipe(
        catchError((err: unknown) => {
          if (err instanceof HttpErrorResponse && err.status === 404) {
            return of(null);
          }
          return throwError(() => err);
        }),
      );
    }

    if (exists) {
      return this.userSettingsApi.updateByName(entry.name, entry.value);
    }

    return this.userSettingsApi.create({ settingName: entry.name, settingValue: entry.value });
  }

  private mapAllSettings(settings: IUserSettingDto[]): UiPreferencesForm {
    const byName = new Map(settings.map((s) => [s.settingName, s.settingValue]));
    return {
      accountingPeriod: this.mapAccountingPeriodSettings(byName),
      ledger: this.mapLedgerDisplaySettings(byName),
      accounts: this.mapAccountsDisplaySettings(byName),
      accountTypes: this.mapAccountTypesDisplaySettings(byName),
      onboarding: {
        welcomeDismissed: isWelcomeDismissed(byName.get(ONBOARDING_SETTING_KEYS.welcomeDismissed)),
      },
      theme: resolveUiThemeId(byName.get(UI_THEME_SETTING_KEY)),
    };
  }

  private mapAccountingPeriodSettings(byName: Map<string, string>): AccountingPeriodSettingsForm {
    const unit = (byName.get(ACCOUNTING_PERIOD_SETTING_KEYS.frequencyUnit) ?? '') as AccountingPeriodFrequencyUnit | '';
    const countRaw = byName.get(ACCOUNTING_PERIOD_SETTING_KEYS.frequencyCount);
    const count = countRaw != null && countRaw !== '' ? Number.parseInt(countRaw, 10) : null;
    const firstStart = byName.get(ACCOUNTING_PERIOD_SETTING_KEYS.firstStartDate) ?? '';

    if (!unit && count == null && !firstStart) {
      return { ...EMPTY_ACCOUNTING_PERIOD_SETTINGS };
    }

    return {
      frequencyUnit: unit,
      frequencyCount: Number.isNaN(count) ? null : count,
      firstStartDate: firstStart,
    };
  }

  private mapLedgerDisplaySettings(byName: Map<string, string>): LedgerDisplaySettingsForm {
    const dateRaw = byName.get(LEDGER_DISPLAY_SETTING_KEYS.dateRange) ?? '';
    const sortRaw = byName.get(LEDGER_DISPLAY_SETTING_KEYS.sortBy) ?? '';
    return {
      dateRange: dateRaw && isLedgerDateRangeOption(dateRaw) ? dateRaw : '',
      sortBy: sortRaw && isLedgerSortByOption(sortRaw) ? sortRaw : '',
    };
  }

  private mapAccountsDisplaySettings(byName: Map<string, string>): AccountsDisplaySettingsForm {
    const sortRaw = byName.get(ACCOUNTS_DISPLAY_SETTING_KEYS.sortBy) ?? '';
    const idsRaw = byName.get(ACCOUNTS_DISPLAY_SETTING_KEYS.accountTypeIds);
    return {
      accountTypeIds: parseAccountTypeIdsJson(idsRaw),
      sortBy: sortRaw && isAccountsSortByOption(sortRaw) ? sortRaw : '',
    };
  }

  private mapAccountTypesDisplaySettings(byName: Map<string, string>): AccountTypesDisplaySettingsForm {
    const sortRaw = byName.get(ACCOUNT_TYPES_DISPLAY_SETTING_KEYS.sortBy) ?? '';
    return {
      sortBy: sortRaw && isAccountTypesSortByOption(sortRaw) ? sortRaw : '',
    };
  }
}
