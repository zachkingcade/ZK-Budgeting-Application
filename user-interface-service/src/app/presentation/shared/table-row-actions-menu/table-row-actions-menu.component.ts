import { ChangeDetectionStrategy, Component, input, output, viewChild, ViewEncapsulation } from '@angular/core';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatMenuModule, MatMenuTrigger } from '@angular/material/menu';
import { ITableRowAction } from './table-row-action.types';

@Component({
  selector: 'app-table-row-actions-menu',
  standalone: true,
  imports: [MatButtonModule, MatIconModule, MatMenuModule],
  templateUrl: './table-row-actions-menu.component.html',
  styleUrl: './table-row-actions-menu.component.scss',
  encapsulation: ViewEncapsulation.None,
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class TableRowActionsMenuComponent {
  readonly actions = input<ITableRowAction[]>([]);
  readonly triggerAriaLabel = input<string>('Open row actions');
  readonly actionSelected = output<{ id: string }>();

  private readonly menuTrigger = viewChild<MatMenuTrigger>('rowActionsMenuTrigger');

  onMenuItemSelected(action: ITableRowAction, ev: MouseEvent): void {
    ev.stopPropagation();
    if (action.disabled === true) {
      return;
    }
    this.menuTrigger()?.closeMenu();
    this.actionSelected.emit({ id: action.id });
  }

  onTriggerClick(ev: MouseEvent): void {
    ev.stopPropagation();
  }
}
