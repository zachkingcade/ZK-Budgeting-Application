import { DatePipe } from '@angular/common';
import { Component, DestroyRef, OnInit, computed, signal } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { RouterLink } from '@angular/router';
import { MatButtonModule } from '@angular/material/button';
import { MatDialog, MatDialogModule } from '@angular/material/dialog';
import { MatIconModule } from '@angular/material/icon';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatTableModule } from '@angular/material/table';
import { PageCage } from '../../page-cage/page-cage.component';
import { AccountingPeriodApplicationService } from '../../../application/accounting-period/accounting-period.application-service';
import {
  IArchivedJournalEntryView,
  IClosedAccountingPeriodListItem,
  INextToCloseView,
} from '../../../adapter/ledger-service/dto/accounting-period/accounting-period.dto';
import { CloseAccountingPeriodDialogComponent } from './close-accounting-period-dialog.component';

@Component({
  selector: 'app-accounting-periods-page',
  standalone: true,
  imports: [
    PageCage,
    DatePipe,
    RouterLink,
    MatButtonModule,
    MatIconModule,
    MatTableModule,
    MatProgressSpinnerModule,
    MatDialogModule,
  ],
  templateUrl: './accounting-periods-page.component.html',
  styleUrl: './accounting-periods-page.component.scss',
})
export class AccountingPeriodsPageComponent implements OnInit {
  protected readonly closedPeriods = signal<IClosedAccountingPeriodListItem[]>([]);
  protected readonly selectedPeriodId = signal<number | null>(null);
  protected readonly archivedEntries = signal<IArchivedJournalEntryView[]>([]);
  protected readonly loadingPeriods = signal(false);
  protected readonly loadingEntries = signal(false);
  protected readonly periodsError = signal<string | null>(null);
  protected readonly entriesError = signal<string | null>(null);
  protected readonly settingsConfigured = signal(true);

  protected readonly displayedColumns = ['entryDate', 'description', 'notes'] as const;

  protected readonly selectedPeriod = computed(() =>
    this.closedPeriods().find((p) => p.closedAccountingPeriodId === this.selectedPeriodId()) ?? null,
  );

  constructor(
    private readonly accountingPeriodService: AccountingPeriodApplicationService,
    private readonly dialog: MatDialog,
    private readonly destroyRef: DestroyRef,
  ) {}

  ngOnInit(): void {
    this.loadClosedPeriods();
  }

  protected selectPeriod(period: IClosedAccountingPeriodListItem): void {
    this.selectedPeriodId.set(period.closedAccountingPeriodId);
    this.loadArchivedEntries(period.closedAccountingPeriodId);
  }

  protected openCloseDialog(): void {
    this.accountingPeriodService
      .getNextToClose()
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: (next) => {
          this.settingsConfigured.set(true);
          const ref = this.dialog.open(CloseAccountingPeriodDialogComponent, {
            data: next,
            width: '32rem',
          });
          ref.afterClosed().subscribe((closed) => {
            if (closed) {
              this.loadClosedPeriods();
            }
          });
        },
        error: () => {
          this.settingsConfigured.set(false);
        },
      });
  }

  protected formatPeriodRange(period: IClosedAccountingPeriodListItem): string {
    return `${period.startDate} – ${period.endDate}`;
  }

  private loadClosedPeriods(): void {
    this.loadingPeriods.set(true);
    this.periodsError.set(null);
    this.accountingPeriodService
      .listClosedPeriods()
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: (periods) => {
          this.closedPeriods.set(periods);
          this.loadingPeriods.set(false);
          if (periods.length > 0 && this.selectedPeriodId() == null) {
            this.selectPeriod(periods[0]);
          }
        },
        error: () => {
          this.periodsError.set('Failed to load closed accounting periods');
          this.loadingPeriods.set(false);
        },
      });
  }

  private loadArchivedEntries(closedPeriodId: number): void {
    this.loadingEntries.set(true);
    this.entriesError.set(null);
    this.accountingPeriodService
      .listArchivedEntries(closedPeriodId)
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: (entries) => {
          this.archivedEntries.set(entries);
          this.loadingEntries.set(false);
        },
        error: () => {
          this.entriesError.set('Failed to load archived journal entries');
          this.loadingEntries.set(false);
        },
      });
  }
}
