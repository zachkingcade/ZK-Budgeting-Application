import { Component, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatDialogModule, MatDialogRef } from '@angular/material/dialog';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatRadioModule } from '@angular/material/radio';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { FeedbackApplicationService } from '../../../application/feedback/feedback.application-service';
import { ToastService } from '../../../application/toast.service';
import { FeedbackType } from '../../../adapter/user-service/dto/feedback.dto';

@Component({
  selector: 'app-submit-feedback-dialog',
  standalone: true,
  imports: [
    MatDialogModule,
    MatButtonModule,
    MatFormFieldModule,
    MatInputModule,
    MatRadioModule,
    MatProgressSpinnerModule,
    FormsModule,
  ],
  templateUrl: './submit-feedback-dialog.component.html',
  styleUrl: './submit-feedback-dialog.component.scss',
})
export class SubmitFeedbackDialogComponent {
  private readonly dialogRef = inject(MatDialogRef<SubmitFeedbackDialogComponent>);
  private readonly feedback = inject(FeedbackApplicationService);
  private readonly toast = inject(ToastService);

  protected feedbackType: FeedbackType = 'bug';
  protected title = '';
  protected message = '';
  protected readonly submitting = signal(false);
  protected readonly errorMessage = signal<string | null>(null);

  protected submit(): void {
    const trimmedTitle = this.title.trim();
    const trimmedMessage = this.message.trim();
    if (!trimmedTitle || !trimmedMessage) {
      this.errorMessage.set('Title and message are required.');
      return;
    }

    this.submitting.set(true);
    this.errorMessage.set(null);
    this.feedback.submit(this.feedbackType, trimmedTitle, trimmedMessage).subscribe({
      next: () => {
        this.submitting.set(false);
        this.toast.showSuccess('Thanks — your feedback was sent.');
        this.dialogRef.close(true);
      },
      error: () => {
        this.submitting.set(false);
        this.errorMessage.set('Could not send feedback. Please try again.');
      },
    });
  }

  protected cancel(): void {
    this.dialogRef.close(false);
  }
}
