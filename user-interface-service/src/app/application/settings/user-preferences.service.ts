import { Injectable } from '@angular/core';
import { Observable, catchError, of, shareReplay, tap } from 'rxjs';
import { DEFAULT_ACCOUNTS_FILTER_STATE } from '../../presentation/ledger/accounts/accounts-filter-state';
import { DEFAULT_ACCOUNT_TYPES_FILTER_STATE } from '../../presentation/ledger/accounts/account-types-filter-state';
import {
  IAccountsFilterState,
  mergeAccountsDisplayDefaults,
} from '../../domain/ui-preferences/accounts-display-settings';
import {
  IAccountTypesFilterState,
  mergeAccountTypesDisplayDefaults,
} from '../../domain/ui-preferences/account-types-display-settings';
import {
  ILedgerFilterSortState,
  mergeLedgerDisplayDefaults,
} from '../../domain/ui-preferences/ledger-display-settings';
import { EMPTY_UI_PREFERENCES, UiPreferencesForm } from '../../domain/ui-preferences/ui-preferences';
import { ThemeService } from '../theme/theme.service';
import { SettingsApplicationService } from './settings.application-service';

const DEFAULT_LEDGER_PAGE_STATE: ILedgerFilterSortState = {
  searchTerm: '',
  selectedDate: 'Last 30 days',
  selectedSortBy: 'Date (Asc.)',
  selectedAccountTypeIds: [],
  selectedAccountIds: [],
};

@Injectable({
  providedIn: 'root',
})
export class UserPreferencesService {
  private cached: UiPreferencesForm | null = null;
  private load$?: Observable<UiPreferencesForm>;

  constructor(
    private readonly settingsService: SettingsApplicationService,
    private readonly themeService: ThemeService,
  ) {}

  ensureLoaded(): Observable<UiPreferencesForm> {
    if (this.cached) {
      return of(this.cached);
    }
    if (!this.load$) {
      this.load$ = this.settingsService.loadUiPreferences().pipe(
        tap((prefs) => {
          this.cached = prefs;
          this.themeService.applyTheme(prefs.theme);
        }),
        catchError(() => {
          this.cached = null;
          this.load$ = undefined;
          return of({ ...EMPTY_UI_PREFERENCES });
        }),
        shareReplay(1),
      );
    }
    return this.load$;
  }

  refresh(): Observable<UiPreferencesForm> {
    this.cached = null;
    this.load$ = undefined;
    return this.ensureLoaded();
  }

  get preferences(): UiPreferencesForm {
    return this.cached ?? EMPTY_UI_PREFERENCES;
  }

  isHydrated(): boolean {
    return this.cached != null;
  }

  getLedgerInitialState(): ILedgerFilterSortState {
    const prefs = this.cached ?? EMPTY_UI_PREFERENCES;
    return mergeLedgerDisplayDefaults(prefs.ledger, DEFAULT_LEDGER_PAGE_STATE);
  }

  getAccountsInitialState(validAccountTypeIds?: readonly number[]): IAccountsFilterState {
    const prefs = this.cached ?? EMPTY_UI_PREFERENCES;
    return mergeAccountsDisplayDefaults(
      prefs.accounts,
      DEFAULT_ACCOUNTS_FILTER_STATE,
      validAccountTypeIds,
    );
  }

  getAccountTypesInitialState(): IAccountTypesFilterState {
    const prefs = this.cached ?? EMPTY_UI_PREFERENCES;
    return mergeAccountTypesDisplayDefaults(prefs.accountTypes, DEFAULT_ACCOUNT_TYPES_FILTER_STATE);
  }
}
