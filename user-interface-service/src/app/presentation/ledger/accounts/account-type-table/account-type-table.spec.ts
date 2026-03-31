import { ComponentFixture, TestBed } from '@angular/core/testing';

import { AccountTypeTable } from './account-type-table';

describe('AccountTypeTable', () => {
  let component: AccountTypeTable;
  let fixture: ComponentFixture<AccountTypeTable>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [AccountTypeTable]
    })
    .compileComponents();

    fixture = TestBed.createComponent(AccountTypeTable);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
