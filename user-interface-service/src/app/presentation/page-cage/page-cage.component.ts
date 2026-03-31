import { Component, HostListener, Input } from '@angular/core';
import { FontAwesomeModule } from '@fortawesome/angular-fontawesome';
import { IconDefinition } from '@fortawesome/fontawesome-svg-core';
import { faGithub, faLinkedin } from '@fortawesome/free-brands-svg-icons';

@Component({
  selector: 'app-page-cage',
  standalone: true,
  imports: [FontAwesomeModule],
  templateUrl: './page-cage.component.html',
  styleUrl: './page-cage.component.scss',
})
export class PageCage {
  @Input() pageName: string = 'Unnamed Page';
  @Input() subText: string = 'Contact admin and report this error';
  sidebarOpen: boolean = false;
  faGithub: IconDefinition = faGithub;
  faLinkedin: IconDefinition = faLinkedin;

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
}
