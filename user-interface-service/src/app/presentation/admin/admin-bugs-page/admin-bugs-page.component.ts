import { Component } from '@angular/core';
import { AdminFeedbackPageComponent } from '../admin-feedback-page/admin-feedback-page.component';

@Component({
  selector: 'app-admin-bugs-page',
  standalone: true,
  imports: [AdminFeedbackPageComponent],
  template: `
    <app-admin-feedback-page
      feedbackType="bug"
      pageName="Bug reports"
      subText="User-submitted bug reports"
    />
  `,
})
export class AdminBugsPageComponent {}
