import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideZonelessChangeDetection } from '@angular/core';
import { ActivatedRoute, convertToParamMap } from '@angular/router';
import { of } from 'rxjs';

import { AccountTypesPageComponent } from './account-types-page.component';
import { AccountTypesApplicationService } from '../../../../application/ledger/account-types.application-service';
import { AccountClassificationsApplicationService } from '../../../../application/ledger/account-classifications.application-service';

describe('AccountTypesPageComponent', () => {
  let component: AccountTypesPageComponent;
  let fixture: ComponentFixture<AccountTypesPageComponent>;
  let accountTypesService: Pick<AccountTypesApplicationService, 'getAll'>;
  let classificationsService: Pick<AccountClassificationsApplicationService, 'getAll'>;

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

  const accountTypesServiceMock: Pick<AccountTypesApplicationService, 'getAll'> = {
    getAll: jasmine.createSpy('getAll').and.returnValue(of({ data: { accountTypeList: [] } } as any)),
  } as any;

  const classificationsServiceMock: Pick<AccountClassificationsApplicationService, 'getAll'> = {
    getAll: jasmine.createSpy('getAll').and.returnValue(
      of({ data: { accountClassificationList: [{ id: 1, description: 'Asset', creditEffect: '+', debitEffect: '+' }] } } as any),
    ),
  } as any;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [AccountTypesPageComponent],
      providers: [
        provideZonelessChangeDetection(),
        { provide: ActivatedRoute, useValue: activatedRouteStub },
      ],
    });

    TestBed.overrideProvider(AccountTypesApplicationService, { useValue: accountTypesServiceMock });
    TestBed.overrideProvider(AccountClassificationsApplicationService, { useValue: classificationsServiceMock });
    TestBed.overrideComponent(AccountTypesPageComponent, {
      set: {
        providers: [
          { provide: AccountTypesApplicationService, useValue: accountTypesServiceMock },
          { provide: AccountClassificationsApplicationService, useValue: classificationsServiceMock },
        ],
      },
    });

    await TestBed.compileComponents();

    accountTypesService = TestBed.inject(AccountTypesApplicationService) as any;
    classificationsService = TestBed.inject(AccountClassificationsApplicationService) as any;

    fixture = TestBed.createComponent(AccountTypesPageComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should load classifications then load account types', () => {
    // HAPPY PATH
    expect(classificationsService.getAll).toHaveBeenCalled();
    expect(accountTypesService.getAll).toHaveBeenCalled();
    expect(component.loadError()).toBeNull();
  });

  it('should still load account types when classifications fetch fails', async () => {
    /*
    NEGATIVE PATH: method=getAll (classifications),
    input=throws,
    expected failure message=none (fallback classification map and continue)
    */
    (classificationsService.getAll as jasmine.Spy).and.returnValue(of({ data: null } as any));
    (accountTypesService.getAll as jasmine.Spy).calls.reset();

    const f = TestBed.createComponent(AccountTypesPageComponent);
    f.detectChanges();
    await f.whenStable();

    expect(accountTypesService.getAll).toHaveBeenCalled();
    f.destroy();
  });
});

