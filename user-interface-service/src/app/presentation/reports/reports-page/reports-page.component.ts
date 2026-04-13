import { Component, DestroyRef, OnInit, signal } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { PageCage } from '../../page-cage/page-cage.component';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatTableModule } from '@angular/material/table';
import { MatDialog, MatDialogModule } from '@angular/material/dialog';
import { CommonModule } from '@angular/common';
import { ReportsApplicationService } from '../../../application/reports/reports.application-service';
import { ToastService } from '../../../application/toast.service';
import { IReportJobMetadata } from '../../../adapter/reporting-service/dto/IReportJobMetadata';
import { HttpErrorResponse } from '@angular/common/http';
import { RequestReportDialogComponent } from './request-report-dialog.component';
import { ReportDetailDialogComponent } from './report-detail-dialog.component';
import { AuthManagerService } from '../../../application/auth/auth-manager.service';

@Component({
  selector: 'app-reports-page',
  standalone: true,
  imports: [
    CommonModule,
    PageCage,
    MatButtonModule,
    MatIconModule,
    MatTableModule,
    MatDialogModule,
  ],
  templateUrl: './reports-page.component.html',
  styleUrl: './reports-page.component.scss',
})
export class ReportsPageComponent implements OnInit {
  constructor(
    private readonly reportsApplicationService: ReportsApplicationService,
    private readonly toastService: ToastService,
    private readonly destroyRef: DestroyRef,
    private readonly dialog: MatDialog,
    private readonly authManager: AuthManagerService,
  ) {}

  readonly reports = signal<IReportJobMetadata[]>([]);
  readonly loading = signal(false);
  readonly loadError = signal<string | null>(null);

  displayedColumns: string[] = ['reportType', 'requestedAt', 'status', 'actions'];

  ngOnInit(): void {
    this.refresh();
  }

  refresh(): void {
    this.loading.set(true);
    this.loadError.set(null);
    this.reportsApplicationService
      .listReports()
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: (res) => {
          this.reports.set(res.data ?? []);
          this.loading.set(false);
        },
        error: (err: unknown) => {
          this.loading.set(false);
          this.loadError.set(this.formatHttpError(err));
        },
      });
  }

  openRequestDialog(): void {
    const ref = this.dialog.open(RequestReportDialogComponent, {
      width: '640px',
      data: {},
    });
    ref.afterClosed().subscribe((submitted: boolean) => {
      if (submitted) {
        this.toastService.showSuccess(
          'Your report was requested. It may take a little time — check back shortly.',
        );
        this.refresh();
      }
    });
  }

  openDetail(row: IReportJobMetadata): void {
    this.dialog.open(ReportDetailDialogComponent, {
      width: '560px',
      data: { jobId: row.id },
    });
  }

  download(row: IReportJobMetadata): void {
    if (row.status !== 'COMPLETED') {
      return;
    }
    this.reportsApplicationService
      .downloadPdf(row.id)
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: (blob: Blob) => {
          const url: string = window.URL.createObjectURL(blob);
          const a: HTMLAnchorElement = document.createElement('a');
          a.href = url;
          a.download = this.buildDownloadFilename(row);
          a.click();
          window.URL.revokeObjectURL(url);
        },
        error: (err: unknown) => {
          this.toastService.showError(this.friendlyError(err));
        },
      });
  }

  private formatHttpError(err: unknown): string {
    return this.friendlyError(err);
  }

  private buildDownloadFilename(row: IReportJobMetadata): string {
    const username: string = this.authManager.getAuthSnapshot().username ?? 'user';
    const reportType: string = row.reportType ?? 'report';
    const createdIso: string = row.completedAt ?? row.requestedAt ?? new Date().toISOString();
    const createdStamp: string = this.formatIsoForFilename(createdIso);
    return `${this.safeSegment(username)}_${this.safeSegment(reportType)}_${createdStamp}.pdf`;
  }

  private safeSegment(raw: string): string {
    const trimmed: string = String(raw ?? '').trim();
    const noSpaces: string = trimmed.replace(/\s+/g, '-');
    const cleaned: string = noSpaces.replace(/[^A-Za-z0-9_-]/g, '');
    return cleaned.length > 0 ? cleaned : 'value';
  }

  private formatIsoForFilename(iso: string): string {
    const d: Date = new Date(iso);
    if (Number.isNaN(d.getTime())) {
      return this.formatDateForFilename(new Date());
    }
    return this.formatDateForFilename(d);
  }

  private formatDateForFilename(d: Date): string {
    const pad2 = (n: number): string => String(n).padStart(2, '0');
    const yyyy: string = String(d.getFullYear());
    const mm: string = pad2(d.getMonth() + 1);
    const dd: string = pad2(d.getDate());
    const hh: string = pad2(d.getHours());
    const min: string = pad2(d.getMinutes());
    const ss: string = pad2(d.getSeconds());
    return `${yyyy}${mm}${dd}-${hh}${min}${ss}`;
  }

  private friendlyError(err: unknown): string {
    if (err instanceof HttpErrorResponse) {
      const body: unknown = err.error;
      if (body && typeof body === 'object' && 'message' in body && typeof (body as { message: string }).message === 'string') {
        return (body as { message: string }).message;
      }
      if (err.status >= 500) {
        return 'Internal error please contact system admin.';
      }
    }
    return 'Internal error please contact system admin.';
  }
}
