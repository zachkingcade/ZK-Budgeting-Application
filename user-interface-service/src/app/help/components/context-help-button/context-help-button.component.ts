import { Component, Input, inject } from '@angular/core';
import { MatButtonModule } from '@angular/material/button';
import { MatDialog } from '@angular/material/dialog';
import { MatIconModule } from '@angular/material/icon';
import { MatTooltipModule } from '@angular/material/tooltip';
import { HelpContentService } from '../../help-content.service';
import {
  ContextHelpDialogComponent,
  ContextHelpDialogData,
} from '../context-help-dialog/context-help-dialog.component';

@Component({
  selector: 'app-context-help-button',
  standalone: true,
  imports: [MatButtonModule, MatIconModule, MatTooltipModule],
  templateUrl: './context-help-button.component.html',
  styleUrl: './context-help-button.component.scss',
})
export class ContextHelpButtonComponent {
  @Input({ required: true }) helpId!: string;
  @Input() label: string | null = null;

  private readonly helpContent = inject(HelpContentService);
  private readonly dialog = inject(MatDialog);

  protected tooltip(): string {
    const content = this.helpContent.getContextHelp(this.helpId);
    return content ? `Help: ${content.title}` : 'Help';
  }

  protected openHelp(event: Event): void {
    event.stopPropagation();
    event.preventDefault();
    const content = this.helpContent.getContextHelp(this.helpId);
    if (!content) {
      return;
    }
    const data: ContextHelpDialogData = { content };
    this.dialog.open(ContextHelpDialogComponent, {
      data,
      width: 'min(28rem, calc(100vw - 2rem))',
      maxWidth: '95vw',
      autoFocus: 'first-tabbable',
    });
  }
}
