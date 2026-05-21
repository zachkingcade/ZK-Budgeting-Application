import { Component } from '@angular/core';
import { AdminFeedbackPageComponent } from '../admin-feedback-page/admin-feedback-page.component';

@Component({
  selector: 'app-admin-suggestions-page',
  standalone: true,
  imports: [AdminFeedbackPageComponent],
  template: `
    <app-admin-feedback-page
      feedbackType="suggestion"
      pageName="Suggestions"
      subText="User-submitted suggestions"
    />
  `,
})
export class AdminSuggestionsPageComponent {}
