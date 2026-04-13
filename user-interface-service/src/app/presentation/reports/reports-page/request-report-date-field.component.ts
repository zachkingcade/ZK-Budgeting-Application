import { Component, input, model } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatDatepickerModule } from '@angular/material/datepicker';
import { MatNativeDateModule } from '@angular/material/core';
import { MatIconModule } from '@angular/material/icon';

/**
 * One scoped MatDatepicker per instance so toggles are not all wired to the same #dp ref
 * when several date parameters appear on a form (e.g. Transaction Summary).
 */
@Component({
  selector: 'app-request-report-date-field',
  standalone: true,
  imports: [
    FormsModule,
    MatFormFieldModule,
    MatInputModule,
    MatDatepickerModule,
    MatNativeDateModule,
    MatIconModule,
  ],
  template: `
    <mat-form-field appearance="outline" class="full">
      <mat-label>{{ label() }}</mat-label>
      <input
        matInput
        [matDatepicker]="dp"
        [ngModel]="value()"
        (ngModelChange)="value.set($event)"
        [name]="nameAttr()"
      />
      <mat-datepicker-toggle matIconSuffix [for]="dp" />
      <mat-datepicker #dp />
    </mat-form-field>
  `,
  styles: `
    :host {
      display: block;
    }
    .full {
      width: 100%;
    }
  `,
})
export class RequestReportDateFieldComponent {
  readonly label = input.required<string>();
  readonly nameAttr = input.required<string>();
  /** Selected calendar value (two-way with parent). */
  readonly value = model<Date | null>(null);
}
