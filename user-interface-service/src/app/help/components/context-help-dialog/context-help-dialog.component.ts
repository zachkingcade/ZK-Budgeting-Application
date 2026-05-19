import { Component, inject } from '@angular/core';
import { MatButtonModule } from '@angular/material/button';
import { MAT_DIALOG_DATA, MatDialogModule, MatDialogRef } from '@angular/material/dialog';
import { RouterLink } from '@angular/router';
import { ContextHelpContent } from '../../models/help-content.model';
import { HelpBodyComponent } from '../help-body/help-body.component';

export interface ContextHelpDialogData {
  content: ContextHelpContent;
}

@Component({
  selector: 'app-context-help-dialog',
  standalone: true,
  imports: [MatDialogModule, MatButtonModule, RouterLink, HelpBodyComponent],
  templateUrl: './context-help-dialog.component.html',
  styleUrl: './context-help-dialog.component.scss',
})
export class ContextHelpDialogComponent {
  protected readonly data = inject<ContextHelpDialogData>(MAT_DIALOG_DATA);
  private readonly dialogRef = inject(MatDialogRef<ContextHelpDialogComponent>);

  protected close(): void {
    this.dialogRef.close();
  }

  protected helpPageLink(): string[] {
    const slug = this.data.content.seeMoreHelpId;
    return slug ? ['/help', slug] : ['/help'];
  }
}
