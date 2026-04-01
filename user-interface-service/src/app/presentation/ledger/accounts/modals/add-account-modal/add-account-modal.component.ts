import { CommonModule } from '@angular/common';
import { Component, DestroyRef, EventEmitter, Input, OnChanges, Output, signal, SimpleChanges } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { MatButtonModule } from '@angular/material/button';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { MtxSelectModule } from '@ng-matero/extensions/select';
import { AccountTypesApplicationService } from '../../../../../application/ledger/account-types.application-service';
import { AccountsApplicationService } from '../../../../../application/ledger/accounts.application-service';
import { POSTCreateAccountRequest } from '../../../../../adapter/ledger-service/dto/account/POSTCreateAccountRequest';

type Option = { id: number; label: string };

@Component({
  selector: 'app-add-account-modal',
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
  templateUrl: './add-account-modal.component.html',
  styleUrl: './add-account-modal.component.scss',
})
export class AddAccountModalComponent implements OnChanges {
  constructor(
    private readonly accountTypesApplicationService: AccountTypesApplicationService,
    private readonly accountsApplicationService: AccountsApplicationService,
    private readonly destroyRef: DestroyRef,
  ) {
    this.loadActiveAccountTypes();
  }

  @Input() open: boolean = false;

  @Output() cancelled = new EventEmitter<void>();
  @Output() created = new EventEmitter<unknown>();

  readonly submitting = signal(false);
  readonly errorMessage = signal<string | null>(null);
  readonly accountTypeOptions = signal<Option[]>([]);

  typeId: number | null = null;
  description: string = '';
  notes: string = '';

  ngOnChanges(changes: SimpleChanges): void {
    if (changes['open'] && this.open) {
      this.resetForm();
      this.errorMessage.set(null);
    }
  }

  private loadActiveAccountTypes(): void {
    this.accountTypesApplicationService
      .getAll()
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: (res) => {
          const list = res.data?.accountTypeList ?? [];
          const options: Option[] = list
            .filter((t) => t.active)
            .map((t) => ({ id: t.id, label: t.description }));
          this.accountTypeOptions.set(options);
        },
      });
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
    if (this.submitting()) return;

    if (this.typeId == null) {
      this.errorMessage.set('Select an account type.');
      return;
    }
    const trimmedDescription = (this.description ?? '').trim();
    if (!trimmedDescription) {
      this.errorMessage.set('Description is required.');
      return;
    }

    const request: POSTCreateAccountRequest = {
      typeId: this.typeId,
      description: trimmedDescription,
      notes: (this.notes ?? '').trim() || undefined,
    };

    this.submitting.set(true);
    this.errorMessage.set(null);

    this.accountsApplicationService
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
          this.errorMessage.set('Could not create account.');
        },
      });
  }

  private resetForm(): void {
    this.typeId = null;
    this.description = '';
    this.notes = '';
  }
}
