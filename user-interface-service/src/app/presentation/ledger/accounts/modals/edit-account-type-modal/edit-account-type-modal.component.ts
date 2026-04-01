import { CommonModule } from '@angular/common';
import { Component, DestroyRef, EventEmitter, Input, OnChanges, Output, signal, SimpleChanges } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { MatButtonModule } from '@angular/material/button';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { AccountTypesApplicationService } from '../../../../../application/ledger/account-types.application-service';
import { POSTUpdateAccountTypeRequest } from '../../../../../adapter/ledger-service/dto/account-types/POSTUpdateAccountTypeRequest';
import { AccountTypeRowView } from '../../account-types-filter-state';

@Component({
  selector: 'app-edit-account-type-modal',
  standalone: true,
  imports: [CommonModule, FormsModule, MatButtonModule, MatFormFieldModule, MatIconModule, MatInputModule],
  templateUrl: './edit-account-type-modal.component.html',
  styleUrl: './edit-account-type-modal.component.scss',
})
export class EditAccountTypeModalComponent implements OnChanges {
  constructor(
    private readonly accountTypesApplicationService: AccountTypesApplicationService,
    private readonly destroyRef: DestroyRef,
  ) {}

  @Input() open: boolean = false;
  @Input() accountType: AccountTypeRowView | null = null;

  @Output() cancelled = new EventEmitter<void>();
  @Output() updated = new EventEmitter<unknown>();

  readonly submitting = signal(false);
  readonly errorMessage = signal<string | null>(null);

  description: string = '';
  notes: string = '';

  ngOnChanges(changes: SimpleChanges): void {
    if ((changes['open'] || changes['accountType']) && this.open && this.accountType) {
      this.description = this.accountType.description;
      this.notes = this.accountType.notes ?? '';
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

  submit(): void {
    if (this.submitting() || !this.accountType) return;

    const trimmedDescription = (this.description ?? '').trim();
    if (!trimmedDescription) {
      this.errorMessage.set('Description is required.');
      return;
    }

    const request: POSTUpdateAccountTypeRequest = {
      id: this.accountType.id,
      description: trimmedDescription,
      notes: (this.notes ?? '').trim() || undefined,
    };

    this.submitting.set(true);
    this.errorMessage.set(null);

    this.accountTypesApplicationService
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
          this.errorMessage.set('Could not update account type.');
        },
      });
  }
}
