import { ComponentFixture, TestBed } from '@angular/core/testing';

import { LedgerSortAndFilterBar } from './ledger-sort-and-filter-bar';

describe('LedgerSortAndFilterBar', () => {
  let component: LedgerSortAndFilterBar;
  let fixture: ComponentFixture<LedgerSortAndFilterBar>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [LedgerSortAndFilterBar]
    })
    .compileComponents();

    fixture = TestBed.createComponent(LedgerSortAndFilterBar);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
