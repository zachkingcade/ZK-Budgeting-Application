import { Component, Inject, signal } from '@angular/core';
import { MAT_DIALOG_DATA, MatDialogModule, MatDialogRef } from '@angular/material/dialog';
import { MatButtonModule } from '@angular/material/button';
import { readLedgerApiErrorMessage } from '../../../adapter/ledger-service/ledger-http-error.util';
import { AccountingPeriodApplicationService } from '../../../application/accounting-period/accounting-period.application-service';
import { INextToCloseView } from '../../../adapter/ledger-service/dto/accounting-period/accounting-period.dto';

@Component({
  selector: 'app-close-accounting-period-dialog',
  standalone: true,
  imports: [MatDialogModule, MatButtonModule],
  template: `
    <h2 mat-dialog-title>Close accounting period</h2>
    <mat-dialog-content>
      <p>
        Period: <strong>{{ data.startDate }}</strong> through <strong>{{ data.endDate }}</strong>
      </p>
      <p class="rules">
        Accounting periods must be closed in chronological order. Once closed, a period cannot be reopened.
        Transactions cannot be posted in closed periods. Pending transactions in this range must be resolved first.
      </p>
      @if (!data.closable) {
        <p class="warning">
          This accounting period ends on <strong>{{ data.closableAfter }}</strong>.
          The current accounting period cannot be closed until that date has passed.
        </p>
      }
      @if (errorMessage()) {
        <p class="error">{{ errorMessage() }}</p>
      }
    </mat-dialog-content>
    <mat-dialog-actions align="end">
      <button mat-button type="button" (click)="dialogRef.close(false)">Cancel</button>
      <button
        mat-raised-button
        color="primary"
        type="button"
        [disabled]="!data.closable || closing()"
        (click)="confirm()"
      >
        Close period
      </button>
    </mat-dialog-actions>
  `,
  styles: [
    `
      .rules,
      .warning {
        font-size: 0.9rem;
        line-height: 1.45;
      }
      .warning {
        color: var(--app-warning-text);
        font-weight: 500;
      }
      .error {
        color: var(--app-error-text-strong);
      }
    `,
  ],
})
export class CloseAccountingPeriodDialogComponent {
  protected readonly closing = signal(false);
  protected readonly errorMessage = signal<string | null>(null);

  constructor(
    @Inject(MAT_DIALOG_DATA) protected readonly data: INextToCloseView,
    protected readonly dialogRef: MatDialogRef<CloseAccountingPeriodDialogComponent, boolean>,
    private readonly accountingPeriodService: AccountingPeriodApplicationService,
  ) {}

  protected confirm(): void {
    this.closing.set(true);
    this.errorMessage.set(null);
    this.accountingPeriodService
      .closePeriod({ startDate: this.data.startDate, endDate: this.data.endDate })
      .subscribe({
        next: () => this.dialogRef.close(true),
        error: (err: unknown) => {
          this.closing.set(false);
          this.errorMessage.set(
            readLedgerApiErrorMessage(err, 'Failed to close accounting period. Please try again.'),
          );
        },
      });
  }
}
