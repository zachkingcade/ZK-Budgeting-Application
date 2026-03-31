import { ComponentFixture, TestBed } from '@angular/core/testing';

import { LedgerPage } from './ledger-page';

describe('LedgerPage', () => {
  let component: LedgerPage;
  let fixture: ComponentFixture<LedgerPage>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [LedgerPage]
    })
    .compileComponents();

    fixture = TestBed.createComponent(LedgerPage);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
