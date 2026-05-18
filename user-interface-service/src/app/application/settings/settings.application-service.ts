import { Injectable } from '@angular/core';
import { Observable, catchError, forkJoin, map, of, switchMap } from 'rxjs';
import { UserSettingsApi } from '../../adapter/user-service/api/user-settings.api';
import { IUserSettingDto } from '../../adapter/user-service/dto/user-settings.dto';
import {
  ACCOUNTING_PERIOD_SETTING_KEYS,
  AccountingPeriodSettingsForm,
  EMPTY_ACCOUNTING_PERIOD_SETTINGS,
  AccountingPeriodFrequencyUnit,
} from '../../domain/accounting-period/accounting-period-settings';

@Injectable({
  providedIn: 'root',
})
export class SettingsApplicationService {
  constructor(private readonly userSettingsApi: UserSettingsApi) {}

  loadAccountingPeriodSettings(): Observable<AccountingPeriodSettingsForm> {
    return this.userSettingsApi.listAll().pipe(
      map((response) => this.mapSettings(response.data ?? [])),
    );
  }

  saveAccountingPeriodSettings(form: AccountingPeriodSettingsForm): Observable<void> {
    const entries: { name: string; value: string }[] = [
      { name: ACCOUNTING_PERIOD_SETTING_KEYS.frequencyUnit, value: form.frequencyUnit },
      { name: ACCOUNTING_PERIOD_SETTING_KEYS.frequencyCount, value: String(form.frequencyCount ?? '') },
      { name: ACCOUNTING_PERIOD_SETTING_KEYS.firstStartDate, value: form.firstStartDate },
    ];

    const upserts = entries.map((entry) =>
      this.userSettingsApi.deleteByName(entry.name).pipe(
        catchError(() => of(null)),
        switchMap(() => this.userSettingsApi.create({ settingName: entry.name, settingValue: entry.value })),
      ),
    );

    return forkJoin(upserts).pipe(map(() => undefined));
  }

  private mapSettings(settings: IUserSettingDto[]): AccountingPeriodSettingsForm {
    const byName = new Map(settings.map((s) => [s.settingName, s.settingValue]));
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
}
