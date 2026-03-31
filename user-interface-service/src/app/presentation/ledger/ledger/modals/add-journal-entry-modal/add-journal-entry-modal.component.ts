import { CommonModule } from '@angular/common';
import { Component, DestroyRef, EventEmitter, Input, OnInit, Output, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { MatButtonModule } from '@angular/material/button';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { MtxSelectModule } from '@ng-matero/extensions/select';
import { AccountsApplicationService } from '../../../../../application/ledger/accounts.application-service';
import { JournalEntryApplicationService } from '../../../../../application/ledger/journal-entry.application-service';
import { POSTCreateJournalEntryRequest } from '../../../../../adapter/ledger-service/dto/journal-entry/POSTCreateJournalEntryRequest';
import { AccountEnrichedObject } from '../../../../../adapter/ledger-service/dto/account/AccountEnrichedObject';
import {
  dollarsStringToMinorUnits,
  IJournalEntryLineDraft,
  validateJournalEntryDraft,
} from '../../../../../domain/journal-entry/journal-entry.validation';

type LedgerOption<TId extends string | number> = { id: TId; label: string };

@Component({
  selector: 'app-add-journal-entry-modal',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule,
    MatButtonModule,
    MatFormFieldModule,
    MatIconModule,
    MatInputModule,
    MtxSelectModule,
  ],
  templateUrl: './add-journal-entry-modal.component.html',
  styleUrl: './add-journal-entry-modal.component.scss',
})
export class AddJournalEntryModalComponent implements OnInit {
  constructor(
    private readonly accountsApplicationService: AccountsApplicationService,
    private readonly journalEntryApplicationService: JournalEntryApplicationService,
    private readonly destroyRef: DestroyRef,
  ) {}

  @Input() open: boolean = false;

  @Output() cancelled: EventEmitter<void> = new EventEmitter<void>();
  @Output() created: EventEmitter<unknown> = new EventEmitter<unknown>();

  readonly submitting = signal<boolean>(false);
  readonly errorMessage = signal<string | null>(null);

  readonly accountOptions = signal<LedgerOption<number>[]>([]);

  entryDate: string = this.toIsoDate(new Date());
  description: string = '';
  notes: string = '';

  readonly lines = signal<IJournalEntryLineDraft[]>([
    { amountDollars: '', accountId: null, direction: '', notes: '' },
    { amountDollars: '', accountId: null, direction: '', notes: '' },
  ]);

  readonly directionOptions: LedgerOption<'C' | 'D'>[] = [
    { id: 'D', label: 'Debit' },
    { id: 'C', label: 'Credit' },
  ];

  ngOnInit(): void {
    this.accountsApplicationService
      .getAll()
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: (res) => {
          const list: AccountEnrichedObject[] = res.data?.accountsList ?? [];
          const options: LedgerOption<number>[] = list
            .filter((a) => a.active)
            .map((a) => ({ id: a.accountId, label: a.accountDisplayName }));
          this.accountOptions.set(options);
        },
      });
  }

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

  addRow(): void {
    this.lines.update((prev) => [...prev, { amountDollars: '', accountId: null, direction: '', notes: '' }]);
  }

  removeRow(index: number): void {
    this.lines.update((prev) => prev.filter((_l, i) => i !== index));
  }

  submit(): void {
    if (this.submitting()) {
      return;
    }

    const trimmedDescription: string = (this.description ?? '').trim();
    if (trimmedDescription.length === 0) {
      this.errorMessage.set('Description is required.');
      return;
    }

    const validation = validateJournalEntryDraft(this.lines());
    if (!validation.valid) {
      this.errorMessage.set(validation.errorMessage);
      return;
    }

    const journalLines = this.lines().map((l) => ({
      amount: dollarsStringToMinorUnits(l.amountDollars) as number,
      accountId: l.accountId as number,
      direction: l.direction,
      notes: (l.notes ?? '').trim() || undefined,
    }));

    const request: POSTCreateJournalEntryRequest = {
      entryDate: this.entryDate,
      description: trimmedDescription,
      notes: (this.notes ?? '').trim() || undefined,
      journalLines,
    };

    this.submitting.set(true);
    this.errorMessage.set(null);

    this.journalEntryApplicationService
      .create(request)
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: (res) => {
          this.submitting.set(false);
          this.created.emit(res);
          this.resetForm();
        },
        error: (err: unknown) => {
          // eslint-disable-next-line no-console
          console.error(err);
          this.submitting.set(false);
          this.errorMessage.set('Could not create journal entry.');
        },
      });
  }

  private resetForm(): void {
    this.entryDate = this.toIsoDate(new Date());
    this.description = '';
    this.notes = '';
    this.lines.set([
      { amountDollars: '', accountId: null, direction: '', notes: '' },
      { amountDollars: '', accountId: null, direction: '', notes: '' },
    ]);
  }

  private toIsoDate(d: Date): string {
    const year: number = d.getFullYear();
    const month: string = String(d.getMonth() + 1).padStart(2, '0');
    const day: string = String(d.getDate()).padStart(2, '0');
    return `${year}-${month}-${day}`;
  }
}

