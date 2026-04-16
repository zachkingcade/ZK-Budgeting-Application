import { ComponentFixture, TestBed } from '@angular/core/testing';

import { LedgerTable } from './ledger-table.component';

describe('LedgerTable', () => {
  let component: LedgerTable;
  let fixture: ComponentFixture<LedgerTable>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [LedgerTable],
    }).compileComponents();

    fixture = TestBed.createComponent(LedgerTable);
    component = fixture.componentInstance;
    fixture.componentRef.setInput('entries', []);
    fixture.componentRef.setInput('loading', false);
    fixture.componentRef.setInput('loadError', null);
    fixture.componentRef.setInput('removingEntryId', null);
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
