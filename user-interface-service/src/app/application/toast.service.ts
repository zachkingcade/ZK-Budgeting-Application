import { Injectable } from '@angular/core';
import { MatSnackBar } from '@angular/material/snack-bar';

@Injectable({
  providedIn: 'root',
})
export class ToastService {
  constructor(private readonly snackBar: MatSnackBar) {}

  showSuccess(message: string): void {
    this.snackBar.open(message, 'Dismiss', { duration: 6000, panelClass: ['toast-success'] });
  }

  showError(message: string): void {
    this.snackBar.open(message, 'Dismiss', { duration: 8000, panelClass: ['toast-error'] });
  }
}
