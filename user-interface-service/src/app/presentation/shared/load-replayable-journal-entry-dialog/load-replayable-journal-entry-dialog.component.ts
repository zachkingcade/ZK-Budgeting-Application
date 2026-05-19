import { DatePipe } from '@angular/common';
import { Component, OnInit, signal } from '@angular/core';
import { MatButtonModule } from '@angular/material/button';
import { MatDialogModule, MatDialogRef } from '@angular/material/dialog';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatTableModule } from '@angular/material/table';
import { ReplayableJournalEntryApplicationService } from '../../../application/planning/replayable-journal-entry.application-service';
import { IReplayableJournalEntryListItem } from '../../../adapter/ledger-service/dto/replayable-journal-entry/replayable-journal-entry.dto';
import { readLedgerApiErrorMessage } from '../../../adapter/ledger-service/ledger-http-error.util';
import { IJournalEntryLineDraft } from '../../../domain/journal-entry/journal-entry.validation';
import { replayableDetailToDrafts } from '../../../domain/replayable-journal-entry/replayable-journal-entry.mapper';
import { ContextHelpButtonComponent } from '../../../help/components/context-help-button/context-help-button.component';

export type LoadReplayableJournalEntryDialogResult = IJournalEntryLineDraft[];

@Component({
  selector: 'app-load-replayable-journal-entry-dialog',
  standalone: true,
  imports: [MatDialogModule, MatButtonModule, MatTableModule, MatProgressSpinnerModule, DatePipe, ContextHelpButtonComponent],
  template: `
    <h2 mat-dialog-title class="help-modal-title">Load replayable journal entry <app-context-help-button helpId="cp-load-replayable" /></h2>
    <mat-dialog-content>
      @if (loading()) {
        <mat-spinner diameter="36"></mat-spinner>
      } @else if (loadError()) {
        <p class="error">{{ loadError() }}</p>
      } @else if (items().length === 0) {
        <p class="empty">No replayable journal entries yet. Create one under Planning.</p>
      } @else {
        <table mat-table [dataSource]="items()" class="listTable">
          <ng-container matColumnDef="replayName">
            <th mat-header-cell *matHeaderCellDef>Name</th>
            <td mat-cell *matCellDef="let row">{{ row.replayName }}</td>
          </ng-container>
          <ng-container matColumnDef="replayLineCount">
            <th mat-header-cell *matHeaderCellDef>Lines</th>
            <td mat-cell *matCellDef="let row">{{ row.replayLineCount }}</td>
          </ng-container>
          <ng-container matColumnDef="lastEditedAt">
            <th mat-header-cell *matHeaderCellDef>Last edited</th>
            <td mat-cell *matCellDef="let row">{{ row.lastEditedAt | date: 'medium' }}</td>
          </ng-container>
          <tr mat-header-row *matHeaderRowDef="displayedColumns"></tr>
          <tr
            mat-row
            *matRowDef="let row; columns: displayedColumns"
            class="listTable__row"
            [class.listTable__row--selected]="selectedId() === row.replayableJournalEntryId"
            (click)="select(row)"
          ></tr>
        </table>
      }
      @if (actionError()) {
        <p class="error">{{ actionError() }}</p>
      }
    </mat-dialog-content>
    <mat-dialog-actions align="end">
      <button mat-button type="button" (click)="dialogRef.close()">Cancel</button>
      <button
        mat-raised-button
        color="primary"
        type="button"
        [disabled]="selectedId() == null || loadingDetail()"
        (click)="confirm()"
      >
        Load
      </button>
    </mat-dialog-actions>
  `,
  styles: [
    `
      .error {
        color: var(--app-error-text-strong);
      }
      .empty {
        color: var(--app-text-muted);
      }
      .listTable {
        width: 100%;
      }
      .listTable__row {
        cursor: pointer;
      }
      .listTable__row--selected {
        background: var(--app-selection-bg);
      }
    `,
  ],
})
export class LoadReplayableJournalEntryDialogComponent implements OnInit {
  protected readonly items = signal<IReplayableJournalEntryListItem[]>([]);
  protected readonly loading = signal(true);
  protected readonly loadingDetail = signal(false);
  protected readonly loadError = signal<string | null>(null);
  protected readonly actionError = signal<string | null>(null);
  protected readonly selectedId = signal<number | null>(null);
  protected readonly displayedColumns = ['replayName', 'replayLineCount', 'lastEditedAt'] as const;

  constructor(
    protected readonly dialogRef: MatDialogRef<
      LoadReplayableJournalEntryDialogComponent,
      LoadReplayableJournalEntryDialogResult | undefined
    >,
    private readonly replayableService: ReplayableJournalEntryApplicationService,
  ) {}

  ngOnInit(): void {
    this.replayableService.list().subscribe({
      next: (list) => {
        this.items.set(list);
        this.loading.set(false);
      },
      error: (err: unknown) => {
        this.loadError.set(readLedgerApiErrorMessage(err, 'Could not load replayable journal entries.'));
        this.loading.set(false);
      },
    });
  }

  protected select(row: IReplayableJournalEntryListItem): void {
    this.selectedId.set(row.replayableJournalEntryId);
    this.actionError.set(null);
  }

  protected confirm(): void {
    const id = this.selectedId();
    if (id == null) {
      return;
    }
    this.loadingDetail.set(true);
    this.actionError.set(null);
    this.replayableService.getById(id).subscribe({
      next: (detail) => {
        this.loadingDetail.set(false);
        this.dialogRef.close(replayableDetailToDrafts(detail));
      },
      error: (err: unknown) => {
        this.loadingDetail.set(false);
        this.actionError.set(readLedgerApiErrorMessage(err, 'Could not load that template.'));
      },
    });
  }
}
