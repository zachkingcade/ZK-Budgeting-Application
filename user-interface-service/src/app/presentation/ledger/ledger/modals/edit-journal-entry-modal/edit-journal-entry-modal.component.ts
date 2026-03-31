import { CommonModule } from '@angular/common';
import { Component, DestroyRef, EventEmitter, Input, Output, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { MatButtonModule } from '@angular/material/button';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { JournalEntryApplicationService } from '../../../../../application/ledger/journal-entry.application-service';
import { JournalEntryDTOEnrichedResponse } from '../../../../../adapter/ledger-service/dto/journal-entry/JournalEntryDTOEnrichedResponse';
import { POSTUpdateJournalEntryRequest } from '../../../../../adapter/ledger-service/dto/journal-entry/POSTUpdateJournalEntryRequest';

interface ILineNoteDraft {
  id: number;
  notes: string;
  display: string;
  direction: string;
  amount: number;
}

@Component({
  selector: 'app-edit-journal-entry-modal',
  standalone: true,
  imports: [CommonModule, FormsModule, MatButtonModule, MatFormFieldModule, MatIconModule, MatInputModule],
  templateUrl: './edit-journal-entry-modal.component.html',
  styleUrl: './edit-journal-entry-modal.component.scss',
})
export class EditJournalEntryModalComponent {
  constructor(
    private readonly journalEntryApplicationService: JournalEntryApplicationService,
    private readonly destroyRef: DestroyRef,
  ) {}

  @Input() open: boolean = false;
  @Input() set entry(value: JournalEntryDTOEnrichedResponse | null) {
    this._entry = value;
    this.hydrateDrafts(value);
  }
  get entry(): JournalEntryDTOEnrichedResponse | null {
    return this._entry;
  }
  private _entry: JournalEntryDTOEnrichedResponse | null = null;

  @Output() cancelled: EventEmitter<void> = new EventEmitter<void>();
  @Output() updated: EventEmitter<unknown> = new EventEmitter<unknown>();

  readonly submitting = signal<boolean>(false);
  readonly errorMessage = signal<string | null>(null);

  description: string = '';
  notes: string = '';
  readonly lineDrafts = signal<ILineNoteDraft[]>([]);

  onBackdropClick(): void {
    if (this.submitting()) {
      return;
    }
    this.onCancel();
  }

  onCancel(): void {
    if (this.submitting()) {
      return;
    }
    this.errorMessage.set(null);
    this.cancelled.emit();
  }

  submit(): void {
    const currentEntry: JournalEntryDTOEnrichedResponse | null = this.entry;
    if (!currentEntry) {
      this.onCancel();
      return;
    }
    if (this.submitting()) {
      return;
    }

    const trimmedDescription: string = (this.description ?? '').trim();
    if (trimmedDescription.length === 0) {
      this.errorMessage.set('Description is required.');
      return;
    }

    const request: POSTUpdateJournalEntryRequest = {
      id: currentEntry.id,
      description: trimmedDescription,
      notes: (this.notes ?? '').trim() || undefined,
      journalLines: this.lineDrafts().map((l) => ({ id: l.id, notes: (l.notes ?? '').trim() })),
    };

    this.submitting.set(true);
    this.errorMessage.set(null);

    this.journalEntryApplicationService
      .update(request)
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: (res) => {
          this.submitting.set(false);
          this.updated.emit(res);
        },
        error: (err: unknown) => {
          // eslint-disable-next-line no-console
          console.error(err);
          this.submitting.set(false);
          this.errorMessage.set('Could not update journal entry.');
        },
      });
  }

  formatMoney(minorUnits: number): string {
    const major: number = minorUnits / 100;
    return new Intl.NumberFormat(undefined, { style: 'currency', currency: 'USD' }).format(major);
  }

  formatDate(isoDate: string): string {
    if (!isoDate) {
      return '';
    }
    const d: Date = new Date(isoDate + 'T12:00:00');
    return new Intl.DateTimeFormat(undefined, { year: 'numeric', month: 'short', day: 'numeric' }).format(d);
  }

  creditTotal(): number {
    const currentEntry: JournalEntryDTOEnrichedResponse | null = this.entry;
    if (!currentEntry) {
      return 0;
    }
    return (currentEntry.journalLines ?? [])
      .filter((l) => l.direction === 'C')
      .reduce((sum, l) => sum + l.amount, 0);
  }

  debitTotal(): number {
    const currentEntry: JournalEntryDTOEnrichedResponse | null = this.entry;
    if (!currentEntry) {
      return 0;
    }
    return (currentEntry.journalLines ?? [])
      .filter((l) => l.direction === 'D')
      .reduce((sum, l) => sum + l.amount, 0);
  }

  private hydrateDrafts(value: JournalEntryDTOEnrichedResponse | null): void {
    if (!value) {
      this.description = '';
      this.notes = '';
      this.lineDrafts.set([]);
      return;
    }
    this.description = value.description ?? '';
    this.notes = value.notes ?? '';
    const drafts: ILineNoteDraft[] = (value.journalLines ?? []).map((l) => ({
      id: l.id,
      notes: l.notes ?? '',
      display: l.accountDisplayName,
      direction: l.direction,
      amount: l.amount,
    }));
    this.lineDrafts.set(drafts);
  }
}

