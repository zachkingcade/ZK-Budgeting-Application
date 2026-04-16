import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideZonelessChangeDetection } from '@angular/core';

import { AccountsTableComponent } from './accounts-table.component';

describe('AccountsTableComponent', () => {
  let component: AccountsTableComponent;
  let fixture: ComponentFixture<AccountsTableComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [AccountsTableComponent],
      providers: [provideZonelessChangeDetection()],
    })
    .compileComponents();

    fixture = TestBed.createComponent(AccountsTableComponent);
    component = fixture.componentInstance;
    fixture.componentRef.setInput('accounts', []);
    fixture.componentRef.setInput('loading', false);
    fixture.componentRef.setInput('loadError', null);
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
