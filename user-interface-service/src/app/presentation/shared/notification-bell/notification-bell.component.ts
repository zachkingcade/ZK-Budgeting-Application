import { DatePipe } from '@angular/common';
import { Component, DestroyRef, inject, signal } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { RouterLink } from '@angular/router';
import { MatButtonModule } from '@angular/material/button';
import { MatMenuModule } from '@angular/material/menu';
import { FontAwesomeModule } from '@fortawesome/angular-fontawesome';
import { faBell } from '@fortawesome/free-solid-svg-icons';
import { NotificationsApplicationService } from '../../../application/notifications/notifications.application-service';
import { NotificationModel } from '../../../domain/notification/notification.model';

@Component({
  selector: 'app-notification-bell',
  standalone: true,
  imports: [FontAwesomeModule, MatMenuModule, MatButtonModule, RouterLink, DatePipe],
  templateUrl: './notification-bell.component.html',
  styleUrl: './notification-bell.component.scss',
})
export class NotificationBellComponent {
  private readonly destroyRef = inject(DestroyRef);
  protected readonly notifications = inject(NotificationsApplicationService);

  protected readonly faBell = faBell;
  protected readonly menuOpen = signal(false);
  protected readonly actionLoading = signal(false);

  protected truncateMessage(message: string, max = 100): string {
    if (message.length <= max) {
      return message;
    }
    return message.slice(0, max);
  }

  protected onMenuOpened(): void {
    this.menuOpen.set(true);
    this.notifications.refreshAll().pipe(takeUntilDestroyed(this.destroyRef)).subscribe({
      error: () => undefined,
    });
  }

  protected onMenuClosed(): void {
    this.menuOpen.set(false);
  }

  protected onNotificationClick(notification: NotificationModel, event: Event): void {
    event.stopPropagation();
    if (notification.seen) {
      return;
    }
    this.notifications.markSeen(notification.id).pipe(takeUntilDestroyed(this.destroyRef)).subscribe({
      error: () => undefined,
    });
  }

  protected clearPreview(): void {
    const ids = this.notifications.previewNotifications().map((n) => n.id);
    if (ids.length === 0) {
      return;
    }
    this.actionLoading.set(true);
    this.notifications
      .clearIds(ids)
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: () => {
          this.notifications.refreshAll().pipe(takeUntilDestroyed(this.destroyRef)).subscribe({
            next: () => this.actionLoading.set(false),
            error: () => this.actionLoading.set(false),
          });
        },
        error: () => this.actionLoading.set(false),
      });
  }
}
