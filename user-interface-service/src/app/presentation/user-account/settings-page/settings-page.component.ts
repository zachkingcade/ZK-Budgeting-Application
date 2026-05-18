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
import {
  AccountingPeriodFrequencyUnit,
  EMPTY_ACCOUNTING_PERIOD_SETTINGS,
  maxFrequencyCount,
} from '../../../domain/accounting-period/accounting-period-settings';

@Component({
  selector: 'app-settings-page',
  standalone: true,
  imports: [
    PageCage,
    ReactiveFormsModule,
    MatExpansionModule,
    MatFormFieldModule,
    MatSelectModule,
    MatOptionModule,
    MatInputModule,
    MatButtonModule,
    MatSnackBarModule,
  ],
  templateUrl: './settings-page.component.html',
  styleUrl: './settings-page.component.scss',
})
export class SettingsPageComponent implements OnInit {
  protected readonly loading = signal(false);
  protected readonly saving = signal(false);
  protected readonly loadError = signal<string | null>(null);

  private savedSnapshot = EMPTY_ACCOUNTING_PERIOD_SETTINGS;

  protected readonly accountingPeriodForm;

  constructor(
    private readonly fb: FormBuilder,
    private readonly settingsService: SettingsApplicationService,
    private readonly snackBar: MatSnackBar,
    private readonly destroyRef: DestroyRef,
  ) {
    this.accountingPeriodForm = this.fb.group({
      frequencyUnit: this.fb.nonNullable.control<AccountingPeriodFrequencyUnit | ''>(''),
      frequencyCount: this.fb.control<number | null>(null, [Validators.required, Validators.min(1)]),
      firstStartDate: this.fb.nonNullable.control('', Validators.required),
    });
  }

  ngOnInit(): void {
    this.loadSettings();
  }

  protected maxCount(): number {
    const unit = this.accountingPeriodForm.controls.frequencyUnit.value;
    if (!unit) {
      return 365;
    }
    return maxFrequencyCount(unit);
  }

  protected save(): void {
    if (this.accountingPeriodForm.invalid) {
      this.accountingPeriodForm.markAllAsTouched();
      return;
    }

    const unit = this.accountingPeriodForm.controls.frequencyUnit.value;
    if (!unit) {
      this.snackBar.open('Select a frequency unit', 'Dismiss', { duration: 4000 });
      return;
    }

    const count = this.accountingPeriodForm.controls.frequencyCount.value;
    if (count == null || count > maxFrequencyCount(unit)) {
      this.snackBar.open('Frequency count is out of range', 'Dismiss', { duration: 4000 });
      return;
    }

    this.saving.set(true);
    this.settingsService
      .saveAccountingPeriodSettings({
        frequencyUnit: unit,
        frequencyCount: count,
        firstStartDate: this.accountingPeriodForm.controls.firstStartDate.value,
      })
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: () => {
          this.savedSnapshot = {
            frequencyUnit: unit,
            frequencyCount: count,
            firstStartDate: this.accountingPeriodForm.controls.firstStartDate.value,
          };
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
  }

  private loadSettings(): void {
    this.loading.set(true);
    this.settingsService
      .loadAccountingPeriodSettings()
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

  private applyForm(settings: typeof EMPTY_ACCOUNTING_PERIOD_SETTINGS): void {
    this.accountingPeriodForm.setValue({
      frequencyUnit: settings.frequencyUnit,
      frequencyCount: settings.frequencyCount,
      firstStartDate: settings.firstStartDate,
    });
  }
}
