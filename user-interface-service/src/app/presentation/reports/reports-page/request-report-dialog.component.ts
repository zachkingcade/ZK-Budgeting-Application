import { Component, computed, OnInit, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { MatDialogModule, MatDialogRef } from '@angular/material/dialog';
import { MatButtonModule } from '@angular/material/button';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { MatIconModule } from '@angular/material/icon';
import { MatCheckboxModule } from '@angular/material/checkbox';
import { RequestReportDateFieldComponent } from './request-report-date-field.component';
import { ReportsApplicationService } from '../../../application/reports/reports.application-service';
import { AccountsApplicationService } from '../../../application/ledger/accounts.application-service';
import { AccountTypesApplicationService } from '../../../application/ledger/account-types.application-service';
import { ICatalogReport, ICatalogField } from '../../../adapter/reporting-service/dto/ICatalogReport';
import { AccountEnrichedObject } from '../../../adapter/ledger-service/dto/account/AccountEnrichedObject';
import { AccountTypeObject } from '../../../adapter/ledger-service/dto/account-types/AccountTypeObject';
import { HttpErrorResponse } from '@angular/common/http';

@Component({
  selector: 'app-request-report-dialog',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule,
    MatDialogModule,
    MatButtonModule,
    MatFormFieldModule,
    MatInputModule,
    MatSelectModule,
    MatIconModule,
    MatCheckboxModule,
    RequestReportDateFieldComponent,
  ],
  templateUrl: './request-report-dialog.component.html',
  styleUrl: './request-report-dialog.component.scss',
})
export class RequestReportDialogComponent implements OnInit {
  constructor(
    private readonly dialogRef: MatDialogRef<RequestReportDialogComponent, boolean>,
    private readonly reportsApplicationService: ReportsApplicationService,
    private readonly accountsApplicationService: AccountsApplicationService,
    private readonly accountTypesApplicationService: AccountTypesApplicationService,
  ) {}

  readonly catalog = signal<ICatalogReport[]>([]);
  readonly accounts = signal<AccountEnrichedObject[]>([]);
  readonly accountTypes = signal<AccountTypeObject[]>([]);
  readonly loading = signal(true);
  readonly error = signal<string | null>(null);

  selectedCode: string | null = null;
  /** Dynamic parameter values by field name */
  paramValues: Record<string, unknown> = {};
  /** Bumped on any parameter change so zoneless CD refreshes submit enabled state. */
  private readonly formRevision = signal(0);

  readonly canSubmit = computed(() => {
    this.formRevision();
    return this.evaluateCanSubmit();
  });

  ngOnInit(): void {
    this.reportsApplicationService.getCatalog().subscribe({
      next: (res) => {
        const raw = res.data;
        const normalized = this.normalizeCatalogFromApi(raw);
        this.catalog.set(normalized);
        this.loading.set(false);
      },
      error: () => {
        this.error.set('Could not load report catalog.');
        this.loading.set(false);
      },
    });
    this.accountsApplicationService.getAll({ filters: { hideInactive: false } }).subscribe({
      next: (r) => this.accounts.set(r.data?.accountsList ?? []),
      error: () => undefined,
    });
    this.accountTypesApplicationService.getAll({ filters: { hideInactive: false } }).subscribe({
      next: (r) => this.accountTypes.set(r.data?.accountTypeList ?? []),
      error: () => undefined,
    });
  }

  selectedReport(): ICatalogReport | null {
    const code: string | null = this.selectedCode;
    if (!code) {
      return null;
    }
    return this.catalog().find((c) => c.code === code) ?? null;
  }

  onSelectCode(code: string | null): void {
    this.selectedCode = code;
    this.paramValues = {};
    if (code) {
      const rep: ICatalogReport | undefined = this.catalog().find((c) => c.code === code);
      if (rep) {
        for (const f of rep.fields) {
          if (f.type === 'checkbox') {
            this.paramValues[f.name] = false;
          }
        }
      }
    }
    this.bumpForm();
  }

  /** MatDatepicker value for a parameter (child field is the source of truth for Date objects). */
  paramDate(name: string): Date | null {
    const v: unknown = this.paramValues[name];
    return v instanceof Date && !Number.isNaN(v.getTime()) ? v : null;
  }

  setParamDate(name: string, value: Date | null): void {
    if (value == null) {
      delete this.paramValues[name];
    } else {
      this.paramValues[name] = value;
    }
    this.bumpForm();
  }

  onParamEdited(): void {
    this.bumpForm();
  }

  submit(): void {
    if (!this.canSubmit()) {
      return;
    }
    const rep: ICatalogReport | null = this.selectedReport();
    if (!rep) {
      return;
    }
    const params: Record<string, unknown> = this.buildParameters(rep.fields);
    this.reportsApplicationService.requestReport(rep.code, params).subscribe({
      next: () => this.dialogRef.close(true),
      error: (err: unknown) => {
        this.error.set(this.formatError(err));
      },
    });
  }

  cancel(): void {
    this.dialogRef.close(false);
  }

  trackField(_i: number, f: ICatalogField): string {
    return f.name;
  }

  /** Stable unique key for @for; avoids NG0955 when code is missing or duplicated after coercion. */
  trackCatalogReport(index: number, c: ICatalogReport): string {
    return c.code ? c.code : `catalog-${index}`;
  }

  private bumpForm(): void {
    this.formRevision.update((n) => n + 1);
  }

  private evaluateCanSubmit(): boolean {
    if (!this.selectedCode) {
      return false;
    }
    const rep: ICatalogReport | null = this.selectedReport();
    if (!rep) {
      return false;
    }
    for (const f of rep.fields) {
      if (this.isFieldRequired(f, rep) && !this.fieldHasValue(f)) {
        return false;
      }
    }
    return true;
  }

  private isFieldRequired(f: ICatalogField, rep: ICatalogReport): boolean {
    if (f.required === true) {
      return true;
    }
    if (rep.code === 'TRANSACTION_SUMMARY' && (f.name === 'dateFrom' || f.name === 'dateTo')) {
      return true;
    }
    return false;
  }

  private fieldHasValue(f: ICatalogField): boolean {
    const v: unknown = this.paramValues[f.name];
    switch (f.type) {
      case 'checkbox':
        return true;
      case 'text':
        return typeof v === 'string' && v.trim().length > 0;
      case 'date':
        return this.hasUsableDate(v);
      case 'account-single':
      case 'account-type-single':
        return v !== undefined && v !== null && v !== '';
      case 'account-multi':
      case 'account-type-multi':
        return Array.isArray(v) && v.length > 0;
      default:
        return v !== undefined && v !== null && v !== '';
    }
  }

  private hasUsableDate(v: unknown): boolean {
    if (v instanceof Date && !Number.isNaN(v.getTime())) {
      return true;
    }
    if (typeof v === 'string' && v.trim().length > 0) {
      return !Number.isNaN(Date.parse(v));
    }
    return false;
  }

  private buildParameters(fields: ICatalogField[]): Record<string, unknown> {
    const out: Record<string, unknown> = {};
    for (const f of fields) {
      const v: unknown = this.paramValues[f.name];
      if (f.type === 'checkbox') {
        out[f.name] = v === true;
        continue;
      }
      if (v === undefined || v === null || v === '') {
        continue;
      }
      if (f.type === 'account-multi' || f.type === 'account-type-multi') {
        if (Array.isArray(v) && v.length > 0) {
          out[f.name] = v;
        }
      } else if (f.type === 'date') {
        const iso: string | null = this.dateToIsoString(v);
        if (iso != null) {
          out[f.name] = iso;
        }
      } else {
        out[f.name] = v;
      }
    }
    return out;
  }

  private dateToIsoString(v: unknown): string | null {
    if (v instanceof Date && !Number.isNaN(v.getTime())) {
      return v.toISOString().slice(0, 10);
    }
    if (typeof v === 'string' && v.trim().length > 0) {
      const d: Date = new Date(v);
      if (!Number.isNaN(d.getTime())) {
        return d.toISOString().slice(0, 10);
      }
    }
    return null;
  }

  /**
   * Ensures @for always receives a real array. Backend may expose JsonNode as a plain object
   * (not Array.isArray), which breaks Angular repeater (Symbol.iterator).
   */
  private normalizeCatalogFromApi(raw: unknown): ICatalogReport[] {
    const rows: unknown[] = this.coerceIterableRows(raw);
    return rows.map((row) => this.normalizeCatalogReport(row));
  }

  private coerceIterableRows(raw: unknown): unknown[] {
    if (raw == null) {
      return [];
    }
    if (Array.isArray(raw)) {
      return raw;
    }
    if (typeof raw === 'object') {
      const obj = raw as Record<string, unknown>;
      // One catalog entry must stay one row; Object.values() would split it into primitives (empty code).
      if (this.looksLikeSingleCatalogReport(obj)) {
        return [obj];
      }
      const keys = Object.keys(obj);
      if (keys.length > 0 && keys.every((k) => /^\d+$/.test(k))) {
        return keys.sort((a, b) => Number(a) - Number(b)).map((k) => obj[k]);
      }
      return Object.values(obj);
    }
    return [];
  }

  private looksLikeSingleCatalogReport(o: Record<string, unknown>): boolean {
    const fields = o['fields'];
    const hasFields =
      Array.isArray(fields) || (fields != null && typeof fields === 'object');
    return hasFields && (typeof o['code'] === 'string' || typeof o['displayName'] === 'string');
  }

  private normalizeCatalogReport(row: unknown): ICatalogReport {
    const r = row as Partial<ICatalogReport>;
    const fieldsRaw = r.fields;
    let fields: ICatalogField[];
    if (Array.isArray(fieldsRaw)) {
      fields = fieldsRaw as ICatalogField[];
    } else if (fieldsRaw != null && typeof fieldsRaw === 'object') {
      fields = Object.values(fieldsRaw as Record<string, ICatalogField>);
    } else {
      fields = [];
    }
    return {
      code: String(r.code ?? ''),
      displayName: String(r.displayName ?? ''),
      description: String(r.description ?? ''),
      fields: fields.map((ff) => this.normalizeCatalogField(ff)),
    };
  }

  private normalizeCatalogField(ff: ICatalogField): ICatalogField {
    const raw = ff as ICatalogField & { required?: unknown };
    return {
      name: ff.name,
      description: ff.description,
      type: ff.type,
      required: raw.required === true,
    };
  }

  private formatError(err: unknown): string {
    if (err instanceof HttpErrorResponse && err.error && typeof err.error === 'object' && 'message' in err.error) {
      return String((err.error as { message: string }).message);
    }
    return 'Internal error please contact system admin.';
  }
}
