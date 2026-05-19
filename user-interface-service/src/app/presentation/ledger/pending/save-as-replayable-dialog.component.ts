import { Component, Inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { MAT_DIALOG_DATA, MatDialogModule, MatDialogRef } from '@angular/material/dialog';
import { MatButtonModule } from '@angular/material/button';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { readLedgerApiErrorMessage } from '../../../adapter/ledger-service/ledger-http-error.util';
import { ReplayableJournalEntryApplicationService } from '../../../application/planning/replayable-journal-entry.application-service';
import { draftsToReplayableLines } from '../../../domain/replayable-journal-entry/replayable-journal-entry.mapper';
import { IJournalEntryLineDraft } from '../../../domain/journal-entry/journal-entry.validation';
import { ContextHelpButtonComponent } from '../../../help/components/context-help-button/context-help-button.component';

export interface SaveAsReplayableDialogData {
  defaultName: string;
  lines: IJournalEntryLineDraft[];
  /** Default true — set false for partial templates (e.g. budget income debit only). */
  requireBalanced?: boolean;
  hint?: string;
}

@Component({
  selector: 'app-save-as-replayable-dialog',
  standalone: true,
  imports: [MatDialogModule, MatButtonModule, MatFormFieldModule, MatInputModule, FormsModule, ContextHelpButtonComponent],
  template: `
    <h2 mat-dialog-title class="help-modal-title">Save as replayable journal entry <app-context-help-button helpId="cp-save-as-replayable" /></h2>
    <mat-dialog-content>
      <p class="hint">{{ data.hint ?? 'Stores the current journal lines (amount, account, direction). Line notes are not saved.' }}</p>
      <mat-form-field appearance="outline" class="nameField">
        <mat-label>Replay name</mat-label>
        <input matInput [(ngModel)]="replayName" name="replayName" />
      </mat-form-field>
      @if (errorMessage()) {
        <p class="error">{{ errorMessage() }}</p>
      }
    </mat-dialog-content>
    <mat-dialog-actions align="end">
      <button mat-button type="button" (click)="dialogRef.close(false)">Cancel</button>
      <button mat-raised-button color="primary" type="button" [disabled]="saving()" (click)="confirm()">
        Save
      </button>
    </mat-dialog-actions>
  `,
  styles: [
    `
      .hint {
        font-size: 0.9rem;
        line-height: 1.45;
        margin: 0 0 1rem;
      }
      .nameField {
        width: 100%;
      }
      .error {
        color: var(--app-error-text-strong);
      }
    `,
  ],
})
export class SaveAsReplayableDialogComponent {
  protected replayName: string;
  protected readonly saving = signal(false);
  protected readonly errorMessage = signal<string | null>(null);

  constructor(
    @Inject(MAT_DIALOG_DATA) protected readonly data: SaveAsReplayableDialogData,
    protected readonly dialogRef: MatDialogRef<SaveAsReplayableDialogComponent, boolean>,
    private readonly replayableService: ReplayableJournalEntryApplicationService,
  ) {
    this.replayName = data.defaultName;
  }

  protected confirm(): void {
    const name = this.replayName.trim();
    if (!name) {
      this.errorMessage.set('Replay name is required.');
      return;
    }
    const requireBalanced = this.data.requireBalanced ?? true;
    const lines = draftsToReplayableLines(this.data.lines);
    if (lines.length === 0) {
      this.errorMessage.set('At least one complete line is required.');
      return;
    }
    if (requireBalanced && lines.length < 2) {
      this.errorMessage.set('At least two complete lines are required.');
      return;
    }
    this.saving.set(true);
    this.errorMessage.set(null);
    this.replayableService.create({ replayName: name, lines, requireBalanced }).subscribe({
      next: () => this.dialogRef.close(true),
      error: (err: unknown) => {
        this.saving.set(false);
        this.errorMessage.set(readLedgerApiErrorMessage(err, 'Could not save replayable journal entry.'));
      },
    });
  }
}
