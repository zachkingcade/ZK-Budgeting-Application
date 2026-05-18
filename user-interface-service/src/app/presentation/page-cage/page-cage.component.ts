import { Component, DestroyRef, HostListener, Input, signal } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { NavigationEnd, Router, RouterLink, RouterLinkActive } from '@angular/router';
import { FontAwesomeModule } from '@fortawesome/angular-fontawesome';
import { IconDefinition } from '@fortawesome/fontawesome-svg-core';
import { faGithub, faLinkedin } from '@fortawesome/free-brands-svg-icons';
import { faRightFromBracket } from '@fortawesome/free-solid-svg-icons';
import { filter } from 'rxjs';
import { AuthManagerService } from '../../application/auth/auth-manager.service';

type PageCageNavSectionId = 'master-ledger' | 'accounts' | 'planning' | 'reporting';

@Component({
  selector: 'app-page-cage',
  standalone: true,
  imports: [FontAwesomeModule, RouterLink, RouterLinkActive],
  templateUrl: './page-cage.component.html',
  styleUrl: './page-cage.component.scss',
})
export class PageCage {
  @Input() pageName: string = 'Unnamed Page';
  @Input() subText: string = 'Contact admin and report this error';
  sidebarOpen: boolean = false;
  faGithub: IconDefinition = faGithub;
  faLinkedin: IconDefinition = faLinkedin;
  faLogout: IconDefinition = faRightFromBracket;

  /** At most one section expanded; synced from route on navigation. */
  protected readonly expandedNavSection = signal<PageCageNavSectionId | null>(null);

  constructor(
    private readonly authManager: AuthManagerService,
    private readonly router: Router,
    private readonly destroyRef: DestroyRef,
  ) {
    this.applyRouteToNavSection(this.router.url);
    this.router.events
      .pipe(
        filter((e): e is NavigationEnd => e instanceof NavigationEnd),
        takeUntilDestroyed(this.destroyRef),
      )
      .subscribe((e: NavigationEnd) => {
        this.applyRouteToNavSection(e.urlAfterRedirects);
      });
  }

  protected isNavSectionExpanded(section: PageCageNavSectionId): boolean {
    return this.expandedNavSection() === section;
  }

  protected toggleNavSection(section: PageCageNavSectionId): void {
    this.expandedNavSection.update((current) => (current === section ? null : section));
  }

  private applyRouteToNavSection(url: string): void {
    const path = url.split('?')[0];
    if (path === '/ledger' || path.startsWith('/ledger/')) {
      this.expandedNavSection.set('master-ledger');
      return;
    }
    if (path === '/pending-journal-entries' || path.startsWith('/pending-journal-entries/')) {
      this.expandedNavSection.set('master-ledger');
      return;
    }
    if (path === '/accounts' || path.startsWith('/accounts/')) {
      this.expandedNavSection.set('accounts');
      return;
    }
    if (path === '/account-types' || path.startsWith('/account-types/')) {
      this.expandedNavSection.set('accounts');
      return;
    }
    if (path === '/reports' || path.startsWith('/reports/')) {
      this.expandedNavSection.set('reporting');
      return;
    }
    if (path === '/budget-planning' || path.startsWith('/budget-planning/')) {
      this.expandedNavSection.set('planning');
      return;
    }
  }

  get username(): string | null {
    return this.authManager.getAuthSnapshot().username;
  }

  openSidebar(): void {
    this.sidebarOpen = true;
  }

  closeSidebar(): void {
    this.sidebarOpen = false;
  }

  toggleSidebar(): void {
    this.sidebarOpen = !this.sidebarOpen;
  }

  @HostListener('document:keydown.escape')
  handleEscape(): void {
    if (this.sidebarOpen) {
      this.closeSidebar();
    }
  }

  logoutClicked(): void {
    this.authManager
      .logout()
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: () => {
          this.closeSidebar();
          void this.router.navigateByUrl('/login');
        },
      });
  }
}
