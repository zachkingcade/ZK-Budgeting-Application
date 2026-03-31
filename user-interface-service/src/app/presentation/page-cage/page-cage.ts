import { Component, HostListener, Input } from '@angular/core';
import { FontAwesomeModule } from '@fortawesome/angular-fontawesome';
import { faGithub, faLinkedin } from '@fortawesome/free-brands-svg-icons';

@Component({
  selector: 'app-page-cage',
  standalone: true,
  imports: [FontAwesomeModule],
  templateUrl: './page-cage.html',
  styleUrl: './page-cage.scss',
})
export class PageCage {
  @Input() pageName = "Unnamed Page";
  @Input() subText = 'Contact admin and report this error';
  sidebarOpen = false;
  faGithub = faGithub;
  faLinkedin = faLinkedin;

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
