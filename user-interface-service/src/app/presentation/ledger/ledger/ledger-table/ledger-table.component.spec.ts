import { ComponentFixture, TestBed } from '@angular/core/testing';
import { of } from 'rxjs';

import { LedgerTable } from './ledger-table.component';
import { JournalEntryApplicationService } from '../../../../application/ledger/journal-entry.application-service';

describe('LedgerTable', () => {
  let component: LedgerTable;
  let fixture: ComponentFixture<LedgerTable>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [LedgerTable],
      providers: [
        {
          provide: JournalEntryApplicationService,
          useValue: {
            getAll: () =>
              of({
                statusMessage: 'ok',
                metaData: {
                  requestDate: '',
                  requestTime: '',
                },
                data: { journalEntryList: [] },
              }),
            removeById: () =>
              of({
                statusMessage: 'ok',
                metaData: { requestDate: '', requestTime: '' },
                data: { removedRecordId: 1 },
              }),
          },
        },
      ],
    }).compileComponents();

    fixture = TestBed.createComponent(LedgerTable);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
