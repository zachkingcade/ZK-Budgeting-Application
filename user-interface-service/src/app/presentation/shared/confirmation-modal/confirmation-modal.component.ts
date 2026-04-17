import { CommonModule } from '@angular/common';
import { Component, EventEmitter, Input, Output } from '@angular/core';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';

@Component({
  selector: 'app-confirmation-modal',
  standalone: true,
  imports: [CommonModule, MatButtonModule, MatIconModule],
  templateUrl: './confirmation-modal.component.html',
  styleUrl: './confirmation-modal.component.scss',
})
export class ConfirmationModalComponent {
  @Input() open: boolean = false;
  @Input() header: string = 'Confirm';
  @Input() message: string = '';
  @Input() errorMessage: string | null = null;
  @Input() confirmLabel: string = 'Confirm';
  @Input() cancelLabel: string = 'Cancel';
  @Input() confirmDisabled: boolean = false;
  /** When false, only the primary action is shown (e.g. OK-only alerts). */
  @Input() showCancelButton: boolean = true;

  @Output() confirmed: EventEmitter<void> = new EventEmitter<void>();
  @Output() cancelled: EventEmitter<void> = new EventEmitter<void>();

  onBackdropClick(): void {
    if (this.confirmDisabled) {
      return;
    }
    this.cancelled.emit();
  }

  onCancel(): void {
    if (this.confirmDisabled) {
      return;
    }
    this.cancelled.emit();
  }

  onConfirm(): void {
    if (this.confirmDisabled) {
      return;
    }
    this.confirmed.emit();
  }
}

