import { Component, DestroyRef, HostListener, Input } from '@angular/core';
import { RouterLink, RouterLinkActive } from '@angular/router';
import { FontAwesomeModule } from '@fortawesome/angular-fontawesome';
import { IconDefinition } from '@fortawesome/fontawesome-svg-core';
import { faGithub, faLinkedin } from '@fortawesome/free-brands-svg-icons';
import { faRightFromBracket } from '@fortawesome/free-solid-svg-icons';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { Router } from '@angular/router';
import { AuthManagerService } from '../../application/auth/auth-manager.service';

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

  constructor(
    private readonly authManager: AuthManagerService,
    private readonly router: Router,
    private readonly destroyRef: DestroyRef,
  ) {}

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
