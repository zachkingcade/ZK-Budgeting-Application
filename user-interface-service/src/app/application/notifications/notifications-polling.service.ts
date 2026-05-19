import { DestroyRef, Injectable, inject } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { Subscription, filter, fromEvent, interval, merge } from 'rxjs';
import { NotificationsApplicationService } from './notifications.application-service';

const POLL_INTERVAL_MS = 30_000;
const ACTIVITY_IDLE_MS = 5 * 60 * 1000;

@Injectable({
  providedIn: 'root',
})
export class NotificationsPollingService {
  private readonly destroyRef = inject(DestroyRef);
  private readonly notifications = inject(NotificationsApplicationService);

  private lastActivityAt = Date.now();
  private tabVisible = true;
  private pollSub: Subscription | null = null;
  private started = false;

  start(): void {
    if (this.started) {
      return;
    }
    this.started = true;
    this.recordActivity();
    this.bindActivityListeners();
    this.bindVisibilityListener();
    this.schedulePolling();
    this.notifications
      .refreshAll()
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({ error: () => undefined });
  }

  stop(): void {
    this.started = false;
    this.pollSub?.unsubscribe();
    this.pollSub = null;
  }

  recordActivity(): void {
    this.lastActivityAt = Date.now();
    if (this.started && !this.pollSub) {
      this.schedulePolling();
      this.notifications.refreshAll().subscribe({ error: () => undefined });
    }
  }

  private bindActivityListeners(): void {
    const events = ['mousemove', 'mousedown', 'keydown', 'scroll', 'touchstart'] as const;
    merge(...events.map((name) => fromEvent(document, name)))
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe(() => this.recordActivity());
  }

  private bindVisibilityListener(): void {
    fromEvent(document, 'visibilitychange')
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe(() => {
        this.tabVisible = document.visibilityState === 'visible';
        if (this.tabVisible && this.isUserActive()) {
          this.schedulePolling();
        } else {
          this.pollSub?.unsubscribe();
          this.pollSub = null;
        }
      });
  }

  private schedulePolling(): void {
    this.pollSub?.unsubscribe();
    if (!this.isUserActive()) {
      this.pollSub = null;
      return;
    }
    this.pollSub = interval(POLL_INTERVAL_MS)
      .pipe(
        filter(() => this.isUserActive()),
        takeUntilDestroyed(this.destroyRef),
      )
      .subscribe(() => {
        if (!this.isUserActive()) {
          this.pollSub?.unsubscribe();
          this.pollSub = null;
          return;
        }
        this.notifications.refreshAll().subscribe({ error: () => undefined });
      });
  }

  private isUserActive(): boolean {
    if (!this.tabVisible) {
      return false;
    }
    return Date.now() - this.lastActivityAt <= ACTIVITY_IDLE_MS;
  }
}
