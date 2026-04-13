import { Component, Inject, OnInit, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MAT_DIALOG_DATA, MatDialogModule, MatDialogRef } from '@angular/material/dialog';
import { MatButtonModule } from '@angular/material/button';
import { ReportsApplicationService } from '../../../application/reports/reports.application-service';
import { IReportJobMetadata } from '../../../adapter/reporting-service/dto/IReportJobMetadata';
import { AuthManagerService } from '../../../application/auth/auth-manager.service';

export interface ReportDetailDialogData {
  jobId: number;
}

@Component({
  selector: 'app-report-detail-dialog',
  standalone: true,
  imports: [CommonModule, MatDialogModule, MatButtonModule],
  templateUrl: './report-detail-dialog.component.html',
})
export class ReportDetailDialogComponent implements OnInit {
  constructor(
    private readonly dialogRef: MatDialogRef<ReportDetailDialogComponent>,
    private readonly reportsApplicationService: ReportsApplicationService,
    private readonly authManager: AuthManagerService,
    @Inject(MAT_DIALOG_DATA) public readonly data: ReportDetailDialogData,
  ) {}

  readonly meta = signal<IReportJobMetadata | null>(null);
  readonly error = signal<string | null>(null);

  ngOnInit(): void {
    this.reportsApplicationService.getReport(this.data.jobId).subscribe({
      next: (res) => this.meta.set(res.data ?? null),
      error: () => this.error.set('Could not load report details.'),
    });
  }

  download(): void {
    const m: IReportJobMetadata | null = this.meta();
    if (!m || m.status !== 'COMPLETED') {
      return;
    }
    this.reportsApplicationService.downloadPdf(m.id).subscribe({
      next: (blob: Blob) => {
        const url: string = window.URL.createObjectURL(blob);
        const a: HTMLAnchorElement = document.createElement('a');
        a.href = url;
        a.download = this.buildDownloadFilename(m);
        a.click();
        window.URL.revokeObjectURL(url);
      },
      error: () => this.error.set('Download failed.'),
    });
  }

  private buildDownloadFilename(meta: IReportJobMetadata): string {
    const username: string = this.authManager.getAuthSnapshot().username ?? 'user';
    const reportType: string = meta.reportType ?? 'report';
    const createdIso: string = meta.completedAt ?? meta.requestedAt ?? new Date().toISOString();
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

  close(): void {
    this.dialogRef.close();
  }
}
