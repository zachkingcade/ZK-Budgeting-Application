import { TestBed } from '@angular/core/testing';
import { provideZonelessChangeDetection } from '@angular/core';

import { HelpContentService } from './help-content.service';

describe('HelpContentService', () => {
  let service: HelpContentService;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [provideZonelessChangeDetection(), HelpContentService],
    });
    service = TestBed.inject(HelpContentService);
  });

  it('loads help index with entries', () => {
    const index = service.getIndex();
    expect(index.entries.length).toBeGreaterThanOrEqual(12);
    expect(index.entries[0].slug).toBeTruthy();
  });

  it('resolves context help by id', () => {
    const cp = service.getContextHelp('cp-ledger-page');
    expect(cp).toBeDefined();
    expect(cp?.title).toBeTruthy();
    expect(cp?.paragraphs.length).toBeGreaterThan(0);
  });

  it('resolves help page by slug and lists pages', () => {
    const page = service.getHelpPage('getting-started');
    expect(page?.slug).toBe('getting-started');
    expect(service.listHelpPages().length).toBe(indexLength(service));
  });
});

function indexLength(service: HelpContentService): number {
  return service.getIndex().entries.length;
}
