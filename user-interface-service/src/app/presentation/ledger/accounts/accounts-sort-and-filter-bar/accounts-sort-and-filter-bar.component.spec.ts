import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideZonelessChangeDetection } from '@angular/core';
import { of } from 'rxjs';

import { AccountsSortAndFilterBarComponent } from './accounts-sort-and-filter-bar.component';
import { AccountTypesApplicationService } from '../../../../application/ledger/account-types.application-service';
import { DEFAULT_ACCOUNTS_FILTER_STATE } from '../accounts-filter-state';

describe('AccountsSortAndFilterBarComponent', () => {
  let component: AccountsSortAndFilterBarComponent;
  let fixture: ComponentFixture<AccountsSortAndFilterBarComponent>;

  const accountTypesServiceMock: Pick<AccountTypesApplicationService, 'getAll'> = {
    getAll: jasmine.createSpy('getAll').and.returnValue(of({ data: { accountTypeList: [] } } as any)),
  } as any;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [AccountsSortAndFilterBarComponent],
      providers: [
        provideZonelessChangeDetection(),
        { provide: AccountTypesApplicationService, useValue: accountTypesServiceMock },
      ],
    });

    await TestBed.compileComponents();

    fixture = TestBed.createComponent(AccountsSortAndFilterBarComponent);
    component = fixture.componentInstance;
    component.state = { ...DEFAULT_ACCOUNTS_FILTER_STATE };
    fixture.detectChanges();
    await fixture.whenStable();
  });

  it('should clear hideActiveOnly when show inactive is turned off', () => {
    component.state = {
      ...DEFAULT_ACCOUNTS_FILTER_STATE,
      showInactive: true,
      hideActiveOnly: true,
    };

    let emitted: typeof DEFAULT_ACCOUNTS_FILTER_STATE | undefined;
    component.stateChanged.subscribe((state) => {
      emitted = state;
    });

    component.onShowInactiveChange(false);

    expect(emitted?.showInactive).toBe(false);
    expect(emitted?.hideActiveOnly).toBe(false);
  });

  it('should enable show inactive when hide active only is turned on', () => {
    component.state = {
      ...DEFAULT_ACCOUNTS_FILTER_STATE,
      showInactive: false,
      hideActiveOnly: false,
    };

    let emitted: typeof DEFAULT_ACCOUNTS_FILTER_STATE | undefined;
    component.stateChanged.subscribe((state) => {
      emitted = state;
    });

    component.onHideActiveOnlyChange(true);

    expect(emitted?.hideActiveOnly).toBe(true);
    expect(emitted?.showInactive).toBe(true);
  });
});
