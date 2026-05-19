import { Component, Inject } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { MAT_DIALOG_DATA, MatDialogModule, MatDialogRef } from '@angular/material/dialog';
import { MatButtonModule } from '@angular/material/button';
import { MatNativeDateModule } from '@angular/material/core';
import { MatDatepickerModule } from '@angular/material/datepicker';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { PendingTransactionObject } from '../../../adapter/ledger-service/dto/pending-transaction/PendingTransactionObject';
import { ContextHelpButtonComponent } from '../../../help/components/context-help-button/context-help-button.component';

export interface ChangePendingDateDialogData {
  transaction: PendingTransactionObject;
}

export interface ChangePendingDateDialogResult {
  transactionDate: string;
}

@Component({
  selector: 'app-change-pending-date-dialog',
  standalone: true,
  imports: [
    MatDialogModule,
    MatButtonModule,
    MatFormFieldModule,
    MatInputModule,
    MatDatepickerModule,
    MatNativeDateModule,
    FormsModule,
    ContextHelpButtonComponent,
  ],
  template: `
    <h2 mat-dialog-title class="help-modal-title">Change pending date <app-context-help-button helpId="cp-change-pending-date" /></h2>
    <mat-dialog-content>
      <p class="txLabel">
        <strong>{{ data.transaction.description }}</strong>
      </p>
      <p class="warning">
        Changing the date will make this transaction differ from the date your bank provided in the import file.
        Only change the date if you are intentionally correcting it.
      </p>
      <mat-form-field appearance="outline" class="dateField">
        <mat-label>Transaction date</mat-label>
        <input matInput [matDatepicker]="picker" [(ngModel)]="selectedDate" name="pendingDate" />
        <mat-datepicker-toggle matIconSuffix [for]="picker" />
        <mat-datepicker #picker />
      </mat-form-field>
    </mat-dialog-content>
    <mat-dialog-actions align="end">
      <button mat-button type="button" (click)="dialogRef.close()">Cancel</button>
      <button
        mat-raised-button
        color="primary"
        type="button"
        [disabled]="!selectedDate"
        (click)="confirm()"
      >
        Change date
      </button>
    </mat-dialog-actions>
  `,
  styles: [
    `
      .txLabel {
        margin: 0 0 0.75rem;
      }
      .warning {
        margin: 0 0 1rem;
        font-size: 0.9rem;
        line-height: 1.45;
        color: var(--app-warning-text);
      }
      .dateField {
        width: 100%;
      }
    `,
  ],
})
export class ChangePendingDateDialogComponent {
  protected selectedDate: Date | null;

  constructor(
    @Inject(MAT_DIALOG_DATA) protected readonly data: ChangePendingDateDialogData,
    protected readonly dialogRef: MatDialogRef<ChangePendingDateDialogComponent, ChangePendingDateDialogResult | undefined>,
  ) {
    this.selectedDate = parseIsoDateLocal(data.transaction.transactionDate);
  }

  protected confirm(): void {
    if (!this.selectedDate) {
      return;
    }
    this.dialogRef.close({ transactionDate: formatIsoDateLocal(this.selectedDate) });
  }
}

function parseIsoDateLocal(iso: string): Date | null {
  if (!iso) {
    return null;
  }
  const d = new Date(iso + 'T12:00:00');
  return Number.isNaN(d.getTime()) ? null : d;
}

function formatIsoDateLocal(d: Date): string {
  const y = d.getFullYear();
  const m = String(d.getMonth() + 1).padStart(2, '0');
  const day = String(d.getDate()).padStart(2, '0');
  return `${y}-${m}-${day}`;
}
