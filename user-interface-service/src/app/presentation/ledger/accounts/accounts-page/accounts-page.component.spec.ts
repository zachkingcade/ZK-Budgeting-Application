import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideZonelessChangeDetection } from '@angular/core';
import { ActivatedRoute, convertToParamMap } from '@angular/router';
import { of, throwError } from 'rxjs';

import { AccountsPageComponent } from './accounts-page.component';
import { AccountsApplicationService } from '../../../../application/ledger/accounts.application-service';
import { AccountTypesApplicationService } from '../../../../application/ledger/account-types.application-service';
import { AccountEnrichedObject } from '../../../../adapter/ledger-service/dto/account/AccountEnrichedObject';

describe('AccountsPageComponent', () => {
  let component: AccountsPageComponent;
  let fixture: ComponentFixture<AccountsPageComponent>;
  let accountsService: Pick<AccountsApplicationService, 'getAll' | 'update'>;

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

  const accountsServiceMock: Pick<AccountsApplicationService, 'getAll' | 'update'> = {
    getAll: jasmine.createSpy('getAll').and.returnValue(of({ data: { accountsList: [] } } as any)),
    update: jasmine.createSpy('update').and.returnValue(of({} as any)),
  } as any;

  const accountTypesServiceMock: Pick<AccountTypesApplicationService, 'getAll'> = {
    getAll: jasmine.createSpy('getAll').and.returnValue(of({ data: { accountTypeList: [] } } as any)),
  } as any;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [AccountsPageComponent],
      providers: [
        provideZonelessChangeDetection(),
        { provide: ActivatedRoute, useValue: activatedRouteStub },
      ],
    });

    TestBed.overrideProvider(AccountsApplicationService, { useValue: accountsServiceMock });
    TestBed.overrideProvider(AccountTypesApplicationService, { useValue: accountTypesServiceMock });
    TestBed.overrideComponent(AccountsPageComponent, {
      set: {
        providers: [
          { provide: AccountsApplicationService, useValue: accountsServiceMock },
          { provide: AccountTypesApplicationService, useValue: accountTypesServiceMock },
        ],
      },
    });

    await TestBed.compileComponents();

    accountsService = TestBed.inject(AccountsApplicationService) as any;
    fixture = TestBed.createComponent(AccountsPageComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should load accounts on init', () => {
    // HAPPY PATH
    expect(accountsService.getAll).toHaveBeenCalled();
    expect(component.accounts()).toEqual([]);
    expect(component.loadError()).toBeNull();
  });

  it('should show modal error when toggle active fails', () => {
    /*
    NEGATIVE PATH: method=update,
    input={id: accountId, active: toggled},
    expected failure message=Could not update account status.
    */
    (accountsService.update as jasmine.Spy).and.returnValue(throwError(() => new Error('fail')));

    const acc: AccountEnrichedObject = {
      accountId: 1,
      typeId: 2,
      description: 'Checking',
      accountTypeName: 'Cash',
      accountDisplayName: 'Checking [Cash]',
      accountBalance: 0,
      active: true,
      notes: '',
      creditEffect: '+',
      debitEffect: '+',
    };

    component.openToggleActiveConfirm(acc);
    component.onToggleActiveConfirmed();

    expect(component.togglingAccountId()).toBeNull();
    expect(component.modalError()).toBe('Could not update account status.');
  });
});

