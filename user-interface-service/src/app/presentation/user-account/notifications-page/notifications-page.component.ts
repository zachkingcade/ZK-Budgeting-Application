import { DatePipe } from '@angular/common';
import { Component, DestroyRef, OnInit, inject, signal } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { FontAwesomeModule } from '@fortawesome/angular-fontawesome';
import { faRotateRight, faTrash } from '@fortawesome/free-solid-svg-icons';
import { PageCage } from '../../page-cage/page-cage.component';
import { NotificationsApplicationService } from '../../../application/notifications/notifications.application-service';
import { NotificationModel } from '../../../domain/notification/notification.model';

@Component({
  selector: 'app-notifications-page',
  standalone: true,
  imports: [
    PageCage,
    DatePipe,
    MatButtonModule,
    MatIconModule,
    MatProgressSpinnerModule,
    FontAwesomeModule,
  ],
  templateUrl: './notifications-page.component.html',
  styleUrl: './notifications-page.component.scss',
})
export class NotificationsPageComponent implements OnInit {
  private readonly destroyRef = inject(DestroyRef);
  protected readonly notifications = inject(NotificationsApplicationService);

  protected readonly faReload = faRotateRight;
  protected readonly faDelete = faTrash;
  protected readonly loadError = signal<string | null>(null);
  protected readonly reloading = signal(false);
  protected readonly clearing = signal(false);

  ngOnInit(): void {
    if (this.notifications.notifications().length === 0) {
      this.reload();
    }
  }

  protected reload(): void {
    this.reloading.set(true);
    this.loadError.set(null);
    this.notifications
      .refreshAll()
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: () => this.reloading.set(false),
        error: () => {
          this.reloading.set(false);
          this.loadError.set('Failed to load notifications.');
        },
      });
  }

  protected onRowClick(notification: NotificationModel): void {
    if (notification.seen) {
      return;
    }
    this.notifications.markSeen(notification.id).pipe(takeUntilDestroyed(this.destroyRef)).subscribe({
      error: () => undefined,
    });
  }

  protected deleteNotification(notification: NotificationModel, event: Event): void {
    event.stopPropagation();
    this.notifications.delete(notification.id).pipe(takeUntilDestroyed(this.destroyRef)).subscribe({
      error: () => undefined,
    });
  }

  protected clearAll(): void {
    this.clearing.set(true);
    this.notifications
      .clearAll()
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: () => this.clearing.set(false),
        error: () => this.clearing.set(false),
      });
  }
}
