import { Component, DestroyRef, inject } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { MatButtonModule } from '@angular/material/button';
import { map } from 'rxjs';
import { PageCage } from '../../../presentation/page-cage/page-cage.component';
import { HelpContentService } from '../../help-content.service';
import { HelpPageContent } from '../../models/help-content.model';
import { HelpBodyComponent } from '../help-body/help-body.component';

@Component({
  selector: 'app-help-page',
  standalone: true,
  imports: [PageCage, RouterLink, MatButtonModule, HelpBodyComponent],
  templateUrl: './help-page.component.html',
  styleUrl: './help-page.component.scss',
})
export class HelpPageComponent {
  private readonly route = inject(ActivatedRoute);
  private readonly helpContent = inject(HelpContentService);
  private readonly destroyRef = inject(DestroyRef);

  protected readonly page$ = this.route.paramMap.pipe(
    map((params) => {
      const slug = params.get('slug');
      if (!slug) {
        return null;
      }
      return this.helpContent.getHelpPage(slug) ?? null;
    }),
  );

  protected page: HelpPageContent | null = null;
  protected notFound = false;

  constructor() {
    this.page$.pipe(takeUntilDestroyed(this.destroyRef)).subscribe((p) => {
      this.page = p;
      this.notFound = p == null;
    });
  }
}
