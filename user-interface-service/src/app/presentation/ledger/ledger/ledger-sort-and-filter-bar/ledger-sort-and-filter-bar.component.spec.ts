import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideZonelessChangeDetection } from '@angular/core';
import { ActivatedRoute, convertToParamMap, provideRouter } from '@angular/router';
import { of } from 'rxjs';

import { LedgerSortAndFilterBar } from './ledger-sort-and-filter-bar.component';
import { AccountsApplicationService } from '../../../../application/ledger/accounts.application-service';
import { AccountTypesApplicationService } from '../../../../application/ledger/account-types.application-service';

describe('LedgerSortAndFilterBar', () => {
  let component: LedgerSortAndFilterBar;
  let fixture: ComponentFixture<LedgerSortAndFilterBar>;

  beforeEach(async () => {
    const activatedRouteStub: Partial<ActivatedRoute> = {
      snapshot: {
        paramMap: convertToParamMap({}),
        queryParamMap: convertToParamMap({}),
      } as any,
      params: of({}),
      queryParams: of({}),
      fragment: of(null),
      data: of({}),
      url: of([] as any),
    };

    const accountsServiceMock: Pick<AccountsApplicationService, 'getAll'> = {
      getAll: jasmine.createSpy('getAll').and.returnValue(of({ data: { accountsList: [] } } as any)),
    } as any;

    const accountTypesServiceMock: Pick<AccountTypesApplicationService, 'getAll'> = {
      getAll: jasmine.createSpy('getAll').and.returnValue(of({ data: { accountTypeList: [] } } as any)),
    } as any;

    await TestBed.configureTestingModule({
      imports: [LedgerSortAndFilterBar],
      providers: [
        provideZonelessChangeDetection(),
        provideRouter([]),
        { provide: ActivatedRoute, useValue: activatedRouteStub },
      ],
    })
    ;

    TestBed.overrideProvider(AccountsApplicationService, { useValue: accountsServiceMock });
    TestBed.overrideProvider(AccountTypesApplicationService, { useValue: accountTypesServiceMock });
    TestBed.overrideComponent(LedgerSortAndFilterBar, {
      set: {
        providers: [
          { provide: AccountsApplicationService, useValue: accountsServiceMock },
          { provide: AccountTypesApplicationService, useValue: accountTypesServiceMock },
        ],
      },
    });

    await TestBed.compileComponents();

    fixture = TestBed.createComponent(LedgerSortAndFilterBar);
    component = fixture.componentInstance;
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
