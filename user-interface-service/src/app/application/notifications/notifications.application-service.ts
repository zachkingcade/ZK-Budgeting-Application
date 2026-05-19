import { Injectable, computed, signal } from '@angular/core';
import { Observable, catchError, map, of, tap, throwError } from 'rxjs';
import { UserNotificationsApi } from '../../adapter/user-service/api/user-notifications.api';
import { INotificationDto } from '../../adapter/user-service/dto/notification.dto';
import { NotificationModel } from '../../domain/notification/notification.model';

@Injectable({
  providedIn: 'root',
})
export class NotificationsApplicationService {
  private readonly notificationsState = signal<NotificationModel[]>([]);
  private readonly loadingState = signal(false);

  readonly notifications = this.notificationsState.asReadonly();
  readonly loading = this.loadingState.asReadonly();
  readonly unreadCount = computed(() => this.notificationsState().filter((n) => !n.seen).length);
  readonly previewNotifications = computed(() => this.notificationsState().slice(0, 3));

  constructor(private readonly api: UserNotificationsApi) {}

  refresh(limit?: number): Observable<void> {
    this.loadingState.set(true);
    return this.api.list(limit).pipe(
      tap((response) => {
        const items = (response.data ?? []).map((dto) => this.toModel(dto));
        if (limit == null) {
          this.notificationsState.set(items);
        } else {
          this.mergePreview(items);
        }
        this.loadingState.set(false);
      }),
      map(() => undefined),
      catchError((err) => {
        this.loadingState.set(false);
        return throwError(() => err);
      }),
    );
  }

  /** Full list fetch used by poller and notifications page. */
  refreshAll(): Observable<void> {
    return this.refresh();
  }

  /** Updates preview rows without replacing the full stored list. */
  refreshPreview(): Observable<void> {
    return this.refresh(3);
  }

  markSeen(id: number): Observable<void> {
    return this.api.markSeen(id).pipe(
      tap((response) => {
        const updated = response.data;
        if (updated) {
          this.patchLocal(this.toModel(updated));
        }
      }),
      map(() => undefined),
    );
  }

  delete(id: number): Observable<void> {
    return this.api.delete(id).pipe(
      tap(() => this.removeLocal(id)),
      map(() => undefined),
    );
  }

  clearIds(ids: number[]): Observable<void> {
    if (ids.length === 0) {
      return of(undefined);
    }
    return this.api.clear(ids).pipe(
      tap(() => {
        const idSet = new Set(ids);
        this.notificationsState.update((list) => list.filter((n) => !idSet.has(n.id)));
      }),
      map(() => undefined),
    );
  }

  clearAll(): Observable<void> {
    return this.api.clearAll().pipe(
      tap(() => this.notificationsState.set([])),
      map(() => undefined),
    );
  }

  private mergePreview(preview: NotificationModel[]): void {
    if (preview.length === 0) {
      return;
    }
    this.notificationsState.update((existing) => {
      const byId = new Map(existing.map((n) => [n.id, n]));
      for (const item of preview) {
        byId.set(item.id, item);
      }
      return [...byId.values()].sort(
        (a, b) => new Date(b.datetime).getTime() - new Date(a.datetime).getTime(),
      );
    });
  }

  private patchLocal(updated: NotificationModel): void {
    this.notificationsState.update((list) =>
      list.map((n) => (n.id === updated.id ? updated : n)),
    );
  }

  private removeLocal(id: number): void {
    this.notificationsState.update((list) => list.filter((n) => n.id !== id));
  }

  private toModel(dto: INotificationDto): NotificationModel {
    return {
      id: dto.id,
      userId: dto.userId,
      datetime: dto.datetime,
      system: dto.system,
      title: dto.title,
      message: dto.message,
      seen: dto.seen,
    };
  }
}
