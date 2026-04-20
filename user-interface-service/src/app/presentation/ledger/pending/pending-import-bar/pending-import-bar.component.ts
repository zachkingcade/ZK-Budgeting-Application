import { CommonModule } from '@angular/common';
import { Component, effect, input, output } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatSelectModule } from '@angular/material/select';
import { ImportFormatObject } from '../../../../adapter/ledger-service/dto/import-format/ImportFormatObject';

type SelectOption<T extends string | number> = { id: T; label: string };

@Component({
  selector: 'app-pending-import-bar',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule,
    MatButtonModule,
    MatIconModule,
    MatFormFieldModule,
    MatProgressSpinnerModule,
    MatSelectModule,
  ],
  templateUrl: './pending-import-bar.component.html',
  styleUrl: './pending-import-bar.component.scss',
})
export class PendingImportBarComponent {
  readonly formats = input.required<ImportFormatObject[]>();
  readonly selectedFormatId = input.required<number | null>();
  readonly selectedFileName = input.required<string | null>();
  readonly importing = input.required<boolean>();

  readonly selectedFormatIdChanged = output<number | null>();
  readonly filePicked = output<File>();
  readonly importClicked = output<void>();

  // IMPORTANT: use a plain property for ngModel two-way binding (avoid overwriting signal inputs)
  localSelectedFormatId: number | null = null;

  constructor() {
    effect(() => {
      this.localSelectedFormatId = this.selectedFormatId();
    });
  }

  get formatOptions(): SelectOption<number>[] {
    return (this.formats() ?? []).map((f) => ({ id: f.formatId, label: `${f.formatName} (${f.formatType})` }));
  }

  onFormatChanged(next: unknown): void {
    const normalized: number | null = this.normalizeSelectedFormatId(next);
    this.selectedFormatIdChanged.emit(normalized);
  }

  onFileInputChanged(evt: Event): void {
    const el = evt.target as HTMLInputElement | null;
    const file: File | null = el?.files?.[0] ?? null;
    if (!file) {
      return;
    }
    this.filePicked.emit(file);
  }

  importDisabled(): boolean {
    return this.importing() || this.selectedFormatId() == null || !this.selectedFileName();
  }

  private normalizeSelectedFormatId(value: unknown): number | null {
    if (value == null) {
      return null;
    }
    if (typeof value === 'number') {
      return Number.isFinite(value) ? value : null;
    }
    if (typeof value === 'string') {
      const trimmed = value.trim();
      if (trimmed.length === 0) return null;
      const parsed = Number(trimmed);
      return Number.isFinite(parsed) ? parsed : null;
    }
    if (typeof value === 'object') {
      const v = value as { id?: unknown; formatId?: unknown };
      if (v.formatId != null) {
        return this.normalizeSelectedFormatId(v.formatId);
      }
      if (v.id != null) {
        return this.normalizeSelectedFormatId(v.id);
      }
    }
    return null;
  }
}

