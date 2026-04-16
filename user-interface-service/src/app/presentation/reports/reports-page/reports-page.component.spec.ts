import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideZonelessChangeDetection } from '@angular/core';
import { MatDialog } from '@angular/material/dialog';
import { HttpErrorResponse } from '@angular/common/http';
import { ActivatedRoute, convertToParamMap } from '@angular/router';
import { of, throwError } from 'rxjs';

import { ReportsPageComponent } from './reports-page.component';
import { ReportsApplicationService } from '../../../application/reports/reports.application-service';
import { ToastService } from '../../../application/toast.service';
import { AuthManagerService } from '../../../application/auth/auth-manager.service';

describe('ReportsPageComponent', () => {
  let component: ReportsPageComponent;
  let fixture: ComponentFixture<ReportsPageComponent>;
  let reportsService: Pick<ReportsApplicationService, 'listReports' | 'downloadPdf'>;

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

  const reportsServiceMock: Pick<ReportsApplicationService, 'listReports' | 'downloadPdf'> = {
    listReports: jasmine.createSpy('listReports').and.returnValue(of({ data: [] } as any)),
    downloadPdf: jasmine.createSpy('downloadPdf').and.returnValue(of(new Blob([new Uint8Array([1])], { type: 'application/pdf' }))),
  } as any;

  const toastMock: Pick<ToastService, 'showError' | 'showSuccess'> = {
    showError: jasmine.createSpy('showError'),
    showSuccess: jasmine.createSpy('showSuccess'),
  };

  const dialogMock: Pick<MatDialog, 'open'> = {
    open: jasmine.createSpy('open').and.returnValue({ afterClosed: () => of(false) } as any),
  } as any;

  const authMock: Pick<AuthManagerService, 'getAuthSnapshot'> = {
    getAuthSnapshot: jasmine.createSpy('getAuthSnapshot').and.returnValue({ username: 'alice' } as any),
  } as any;

  beforeEach(async () => {
    (reportsServiceMock.listReports as jasmine.Spy).and.returnValue(of({ data: [] } as any));
    (reportsServiceMock.listReports as jasmine.Spy).calls.reset();
    (reportsServiceMock.downloadPdf as jasmine.Spy).and.returnValue(
      of(new Blob([new Uint8Array([1])], { type: 'application/pdf' })),
    );
    (reportsServiceMock.downloadPdf as jasmine.Spy).calls.reset();

    await TestBed.configureTestingModule({
      imports: [ReportsPageComponent],
      providers: [
        provideZonelessChangeDetection(),
        { provide: ToastService, useValue: toastMock },
        { provide: MatDialog, useValue: dialogMock },
        { provide: AuthManagerService, useValue: authMock },
        { provide: ActivatedRoute, useValue: activatedRouteStub },
      ],
    });

    TestBed.overrideProvider(ReportsApplicationService, { useValue: reportsServiceMock });
    TestBed.overrideComponent(ReportsPageComponent, {
      set: {
        providers: [{ provide: ReportsApplicationService, useValue: reportsServiceMock }],
      },
    });

    await TestBed.compileComponents();

    reportsService = TestBed.inject(ReportsApplicationService) as any;

    fixture = TestBed.createComponent(ReportsPageComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should refresh on init', () => {
    // HAPPY PATH
    expect(reportsService.listReports).toHaveBeenCalled();
    expect(component.loading()).toBeFalse();
    expect(component.loadError()).toBeNull();
  });

  it('should set loadError when listReports fails', () => {
    /*
    NEGATIVE PATH: method=listReports,
    input=HttpErrorResponse 500,
    expected failure message=Internal error please contact system admin.
    */
    (reportsService.listReports as jasmine.Spy).and.returnValue(
      throwError(() => new HttpErrorResponse({ status: 500, statusText: 'Server error' })),
    );

    component.refresh();

    expect(component.loading()).toBeFalse();
    expect(component.loadError()).toBe('Internal error please contact system admin.');
  });

  it('should not download when status is not COMPLETED', () => {
    /*
    NEGATIVE PATH: method=download,
    input={status: QUEUED},
    expected failure message=none (no-op)
    */
    component.download({ id: 1, reportType: 'X', requestedAt: '', status: 'QUEUED' } as any);
    expect(reportsService.downloadPdf).not.toHaveBeenCalled();
  });
});

