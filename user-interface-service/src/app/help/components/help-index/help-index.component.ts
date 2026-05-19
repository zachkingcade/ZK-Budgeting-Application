import { Component, inject } from '@angular/core';
import { RouterLink } from '@angular/router';
import { MatListModule } from '@angular/material/list';
import { PageCage } from '../../../presentation/page-cage/page-cage.component';
import { HelpContentService } from '../../help-content.service';

@Component({
  selector: 'app-help-index',
  standalone: true,
  imports: [PageCage, RouterLink, MatListModule],
  templateUrl: './help-index.component.html',
  styleUrl: './help-index.component.scss',
})
export class HelpIndexComponent {
  private readonly helpContent = inject(HelpContentService);

  protected readonly entries = this.helpContent.getIndex().entries;
}
