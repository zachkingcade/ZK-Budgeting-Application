import { ComponentFixture, TestBed } from '@angular/core/testing';

import { LedgerPage } from './ledger-page.component';

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
