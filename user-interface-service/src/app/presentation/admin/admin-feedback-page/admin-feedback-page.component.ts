import { DatePipe } from '@angular/common';
import { Component, DestroyRef, Input, OnInit, inject, signal } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { FontAwesomeModule } from '@fortawesome/angular-fontawesome';
import { faRotateRight, faTrash } from '@fortawesome/free-solid-svg-icons';
import { PageCage } from '../../page-cage/page-cage.component';
import { FeedbackApplicationService } from '../../../application/feedback/feedback.application-service';
import { FeedbackModel } from '../../../domain/feedback/feedback.model';
import { FeedbackType } from '../../../adapter/user-service/dto/feedback.dto';

@Component({
  selector: 'app-admin-feedback-page',
  standalone: true,
  imports: [
    PageCage,
    DatePipe,
    MatButtonModule,
    MatIconModule,
    MatProgressSpinnerModule,
    FontAwesomeModule,
  ],
  templateUrl: './admin-feedback-page.component.html',
  styleUrl: './admin-feedback-page.component.scss',
})
export class AdminFeedbackPageComponent implements OnInit {
  @Input({ required: true }) feedbackType!: FeedbackType;
  @Input({ required: true }) pageName!: string;
  @Input({ required: true }) subText!: string;

  private readonly destroyRef = inject(DestroyRef);
  private readonly feedbackService = inject(FeedbackApplicationService);

  protected readonly faReload = faRotateRight;
  protected readonly faDelete = faTrash;
  protected readonly items = signal<FeedbackModel[]>([]);
  protected readonly loadError = signal<string | null>(null);
  protected readonly reloading = signal(false);

  ngOnInit(): void {
    this.reload();
  }

  protected reload(): void {
    this.reloading.set(true);
    this.loadError.set(null);
    this.feedbackService
      .list(this.feedbackType)
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: (rows) => {
          this.items.set(rows);
          this.reloading.set(false);
        },
        error: () => {
          this.reloading.set(false);
          this.loadError.set('Failed to load feedback.');
        },
      });
  }

  protected onRowClick(item: FeedbackModel): void {
    if (item.seen) {
      return;
    }
    this.feedbackService.markSeen(item.id).pipe(takeUntilDestroyed(this.destroyRef)).subscribe({
      next: (updated) => {
        this.items.update((list) => list.map((row) => (row.id === updated.id ? updated : row)));
      },
      error: () => undefined,
    });
  }

  protected deleteItem(item: FeedbackModel, event: Event): void {
    event.stopPropagation();
    this.feedbackService.delete(item.id).pipe(takeUntilDestroyed(this.destroyRef)).subscribe({
      next: () => this.items.update((list) => list.filter((row) => row.id !== item.id)),
      error: () => undefined,
    });
  }
}
