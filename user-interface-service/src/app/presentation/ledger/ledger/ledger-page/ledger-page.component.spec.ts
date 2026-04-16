import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideZonelessChangeDetection } from '@angular/core';
import { ActivatedRoute, convertToParamMap } from '@angular/router';
import { of, throwError } from 'rxjs';

import { LedgerPage } from './ledger-page.component';
import { JournalEntryApplicationService } from '../../../../application/ledger/journal-entry.application-service';
import { AccountsApplicationService } from '../../../../application/ledger/accounts.application-service';
import { AccountTypesApplicationService } from '../../../../application/ledger/account-types.application-service';
import { JournalEntryDTOEnrichedResponse } from '../../../../adapter/ledger-service/dto/journal-entry/JournalEntryDTOEnrichedResponse';

describe('LedgerPage', () => {
  let component: LedgerPage;
  let fixture: ComponentFixture<LedgerPage>;
  let journalEntries: Pick<JournalEntryApplicationService, 'getAll' | 'removeById'>;

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

  const journalEntriesMock: Pick<JournalEntryApplicationService, 'getAll' | 'removeById'> = {
    getAll: jasmine.createSpy('getAll').and.returnValue(of({ data: { journalEntryList: [] } } as any)),
    removeById: jasmine.createSpy('removeById').and.returnValue(of(void 0 as any)),
  } as any;

  const accountsServiceMock: Pick<AccountsApplicationService, 'getAll'> = {
    getAll: jasmine.createSpy('getAll').and.returnValue(of({ data: { accountsList: [] } } as any)),
  } as any;

  const accountTypesServiceMock: Pick<AccountTypesApplicationService, 'getAll'> = {
    getAll: jasmine.createSpy('getAll').and.returnValue(of({ data: { accountTypeList: [] } } as any)),
  } as any;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [LedgerPage],
      providers: [
        provideZonelessChangeDetection(),
        { provide: ActivatedRoute, useValue: activatedRouteStub },
      ],
    })
    ;

    TestBed.overrideProvider(JournalEntryApplicationService, { useValue: journalEntriesMock });
    TestBed.overrideProvider(AccountsApplicationService, { useValue: accountsServiceMock });
    TestBed.overrideProvider(AccountTypesApplicationService, { useValue: accountTypesServiceMock });
    TestBed.overrideComponent(LedgerPage, {
      set: {
        providers: [
          { provide: JournalEntryApplicationService, useValue: journalEntriesMock },
          { provide: AccountsApplicationService, useValue: accountsServiceMock },
          { provide: AccountTypesApplicationService, useValue: accountTypesServiceMock },
        ],
      },
    });

    await TestBed.compileComponents();

    journalEntries = TestBed.inject(JournalEntryApplicationService) as any;

    fixture = TestBed.createComponent(LedgerPage);
    component = fixture.componentInstance;
    fixture.detectChanges();
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should load entries on init', () => {
    // HAPPY PATH
    expect(journalEntries.getAll).toHaveBeenCalled();
    expect(component.entries()).toEqual([]);
    expect(component.loadError()).toBeNull();
  });

  it('should set modal error when delete fails', async () => {
    /*
    NEGATIVE PATH: method=removeById,
    input={id: entry.id},
    expected failure message=Could not remove that journal entry.
    */
    (journalEntries.getAll as jasmine.Spy).calls.reset();
    (journalEntries.removeById as jasmine.Spy).and.returnValue(throwError(() => new Error('fail')));

    const entry: JournalEntryDTOEnrichedResponse = {
      id: 1,
      entryDate: '2026-01-01',
      description: 'Test',
      notes: '',
      journalLines: [],
    };

    component.openDeleteConfirm(entry);
    component.onDeleteConfirmed();
    await fixture.whenStable();

    expect(component.removingEntryId()).toBeNull();
    expect(component.modalError()).toBe('Could not remove that journal entry.');
  });
});
