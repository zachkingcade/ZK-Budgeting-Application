import { CommonModule } from '@angular/common';
import { Component, DestroyRef, EventEmitter, Input, OnChanges, Output, signal, SimpleChanges } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { MatButtonModule } from '@angular/material/button';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { AccountsApplicationService } from '../../../../../application/ledger/accounts.application-service';
import { AccountEnrichedObject } from '../../../../../adapter/ledger-service/dto/account/AccountEnrichedObject';
import { POSTUpdateAccountRequest } from '../../../../../adapter/ledger-service/dto/account/POSTUpdateAccountRequest';

@Component({
  selector: 'app-edit-account-modal',
  standalone: true,
  imports: [CommonModule, FormsModule, MatButtonModule, MatFormFieldModule, MatIconModule, MatInputModule],
  templateUrl: './edit-account-modal.component.html',
  styleUrl: './edit-account-modal.component.scss',
})
export class EditAccountModalComponent implements OnChanges {
  constructor(
    private readonly accountsApplicationService: AccountsApplicationService,
    private readonly destroyRef: DestroyRef,
  ) {}

  @Input() open: boolean = false;
  @Input() account: AccountEnrichedObject | null = null;

  @Output() cancelled = new EventEmitter<void>();
  @Output() updated = new EventEmitter<unknown>();

  readonly submitting = signal(false);
  readonly errorMessage = signal<string | null>(null);

  description: string = '';
  notes: string = '';

  ngOnChanges(changes: SimpleChanges): void {
    if ((changes['open'] || changes['account']) && this.open && this.account) {
      this.description = this.account.description;
      this.notes = this.account.notes ?? '';
      this.errorMessage.set(null);
    }
  }

  onBackdropClick(): void {
    if (this.submitting()) return;
    this.onCancel();
  }

  onCancel(): void {
    if (this.submitting()) return;
    this.errorMessage.set(null);
    this.cancelled.emit();
  }

  formatMoney(minorUnits: number): string {
    const major = minorUnits / 100;
    return new Intl.NumberFormat(undefined, { style: 'currency', currency: 'USD' }).format(major);
  }

  submit(): void {
    if (this.submitting() || !this.account) return;

    const trimmedDescription = (this.description ?? '').trim();
    if (!trimmedDescription) {
      this.errorMessage.set('Description is required.');
      return;
    }

    const request: POSTUpdateAccountRequest = {
      id: this.account.accountId,
      description: trimmedDescription,
      notes: (this.notes ?? '').trim() || undefined,
    };

    this.submitting.set(true);
    this.errorMessage.set(null);

    this.accountsApplicationService
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
          this.errorMessage.set('Could not update account.');
        },
      });
  }
}
