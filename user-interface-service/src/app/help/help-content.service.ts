import { Injectable } from '@angular/core';
import helpIndex from './data/help-index.json';
import { CP_BY_ID } from './data/cp-registry';
import { HP_BY_SLUG } from './data/hp-registry';
import { ContextHelpContent, HelpIndex, HelpPageContent } from './models/help-content.model';

@Injectable({
  providedIn: 'root',
})
export class HelpContentService {
  private readonly index = helpIndex as HelpIndex;

  getIndex(): HelpIndex {
    return this.index;
  }

  getContextHelp(id: string): ContextHelpContent | undefined {
    return CP_BY_ID[id];
  }

  getHelpPage(slug: string): HelpPageContent | undefined {
    return HP_BY_SLUG[slug];
  }

  listHelpPages(): HelpPageContent[] {
    return this.index.entries
      .map((e) => this.getHelpPage(e.slug))
      .filter((p): p is HelpPageContent => p != null);
  }
}
