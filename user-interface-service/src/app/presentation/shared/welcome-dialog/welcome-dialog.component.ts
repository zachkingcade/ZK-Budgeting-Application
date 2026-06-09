import { Component, inject } from '@angular/core';
import { RouterLink } from '@angular/router';
import { MatButtonModule } from '@angular/material/button';
import { MatCheckboxModule } from '@angular/material/checkbox';
import { MatDialogModule, MatDialogRef } from '@angular/material/dialog';
import { FormsModule } from '@angular/forms';

export interface WelcomeDialogResult {
  dontShowAgain: boolean;
}

@Component({
  selector: 'app-welcome-dialog',
  standalone: true,
  imports: [MatDialogModule, MatButtonModule, MatCheckboxModule, FormsModule, RouterLink],
  templateUrl: './welcome-dialog.component.html',
  styleUrl: './welcome-dialog.component.scss',
})
export class WelcomeDialogComponent {
  private readonly dialogRef = inject(MatDialogRef<WelcomeDialogComponent, WelcomeDialogResult>);

  protected dontShowAgain = false;

  protected readonly contactEmail = 'zachkingcade@gmail.com';

  protected ok(): void {
    this.dialogRef.close({ dontShowAgain: this.dontShowAgain });
  }

  protected closeWithoutSaving(): void {
    this.dialogRef.close();
  }
}
