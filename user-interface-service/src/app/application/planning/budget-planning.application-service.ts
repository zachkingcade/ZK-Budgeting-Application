import { Injectable } from '@angular/core';
import { Observable, catchError, tap, throwError } from 'rxjs';
import { ApiResponse } from '../../adapter/ledger-service/dto/ApiResponse';
import { BudgetPlanningApi } from '../../adapter/ledger-service/api/budget-planning.api';
import {
  IBudgetPlanEditorView,
  IBudgetPlanListItem,
  IBpBudgetResponse,
  IBpFundingLineResponse,
  IBpIncomeResponse,
  IBpRecurringPayableResponse,
  ICreateBpBudgetRequest,
  ICreateBpFundingLineRequest,
  ICreateBpIncomeRequest,
  ICreateBpRecurringPayableRequest,
  ICreateBudgetPlanRequest,
  IDuplicateBudgetPlanRequest,
  IRenameBudgetPlanRequest,
  IUpdateBpBudgetRequest,
  IUpdateBpFundingLineRequest,
  IUpdateBpIncomeRequest,
  IUpdateBpRecurringPayableRequest,
} from '../../adapter/ledger-service/dto/budget-planning/budget-planning.dto';
import { LedgerApplicationLoggerService } from '../ledger/ledger-application-logger.service';

@Injectable({
  providedIn: 'root',
})
export class BudgetPlanningApplicationService {
  constructor(
    private readonly api: BudgetPlanningApi,
    private readonly logger: LedgerApplicationLoggerService,
  ) {}

  listPlans(): Observable<ApiResponse<IBudgetPlanListItem[]>> {
    const op = 'BudgetPlanningApplicationService.listPlans';
    const t0 = Date.now();
    this.logger.debug(`Starting ${op}`);
    return this.api.listPlans().pipe(
      tap((r) => this.logger.debug(`Ending ${op} (${Date.now() - t0}ms)`, r.statusMessage)),
      catchError((e) => {
        this.logger.error(`Failed ${op} (${Date.now() - t0}ms)`, e);
        return throwError(() => e);
      }),
    );
  }

  createPlan(body: ICreateBudgetPlanRequest): Observable<ApiResponse<IBudgetPlanListItem>> {
    const op = 'BudgetPlanningApplicationService.createPlan';
    const t0 = Date.now();
    this.logger.debug(`Starting ${op}`, body);
    return this.api.createPlan(body).pipe(
      tap((r) => this.logger.debug(`Ending ${op} (${Date.now() - t0}ms)`, r.statusMessage)),
      catchError((e) => {
        this.logger.error(`Failed ${op} (${Date.now() - t0}ms)`, e, body);
        return throwError(() => e);
      }),
    );
  }

  duplicatePlan(
    planId: number,
    body: IDuplicateBudgetPlanRequest,
  ): Observable<ApiResponse<IBudgetPlanListItem>> {
    return this.wrap('duplicatePlan', () => this.api.duplicatePlan(planId, body), body);
  }

  renamePlan(planId: number, body: IRenameBudgetPlanRequest): Observable<ApiResponse<IBudgetPlanListItem>> {
    const op = 'BudgetPlanningApplicationService.renamePlan';
    const t0 = Date.now();
    return this.api.renamePlan(planId, body).pipe(
      tap((r) => this.logger.debug(`Ending ${op} (${Date.now() - t0}ms)`, r.statusMessage)),
      catchError((e) => {
        this.logger.error(`Failed ${op} (${Date.now() - t0}ms)`, e, body);
        return throwError(() => e);
      }),
    );
  }

  deletePlan(planId: number): Observable<ApiResponse<unknown>> {
    const op = 'BudgetPlanningApplicationService.deletePlan';
    const t0 = Date.now();
    return this.api.deletePlan(planId).pipe(
      tap(() => this.logger.debug(`Ending ${op} (${Date.now() - t0}ms)`)),
      catchError((e) => {
        this.logger.error(`Failed ${op} (${Date.now() - t0}ms)`, e);
        return throwError(() => e);
      }),
    );
  }

  loadEditor(planId: number): Observable<ApiResponse<IBudgetPlanEditorView>> {
    const op = 'BudgetPlanningApplicationService.loadEditor';
    const t0 = Date.now();
    return this.api.loadEditor(planId).pipe(
      tap((r) => this.logger.debug(`Ending ${op} (${Date.now() - t0}ms)`, r.statusMessage)),
      catchError((e) => {
        this.logger.error(`Failed ${op} (${Date.now() - t0}ms)`, e);
        return throwError(() => e);
      }),
    );
  }

  createIncome(planId: number, body: ICreateBpIncomeRequest): Observable<ApiResponse<IBpIncomeResponse>> {
    return this.wrap('createIncome', () => this.api.createIncome(planId, body), body);
  }

  updateIncome(
    planId: number,
    incomeId: number,
    body: IUpdateBpIncomeRequest,
  ): Observable<ApiResponse<IBpIncomeResponse>> {
    return this.wrap('updateIncome', () => this.api.updateIncome(planId, incomeId, body), body);
  }

  deleteIncome(planId: number, incomeId: number): Observable<ApiResponse<unknown>> {
    return this.wrap('deleteIncome', () => this.api.deleteIncome(planId, incomeId), { planId, incomeId });
  }

  createRecurringPayable(
    planId: number,
    body: ICreateBpRecurringPayableRequest,
  ): Observable<ApiResponse<IBpRecurringPayableResponse>> {
    return this.wrap('createRecurringPayable', () => this.api.createRecurringPayable(planId, body), body);
  }

  updateRecurringPayable(
    planId: number,
    payableId: number,
    body: IUpdateBpRecurringPayableRequest,
  ): Observable<ApiResponse<IBpRecurringPayableResponse>> {
    return this.wrap('updateRecurringPayable', () => this.api.updateRecurringPayable(planId, payableId, body), body);
  }

  deleteRecurringPayable(planId: number, payableId: number): Observable<ApiResponse<unknown>> {
    return this.wrap('deleteRecurringPayable', () => this.api.deleteRecurringPayable(planId, payableId), {
      planId,
      payableId,
    });
  }

  createBudget(planId: number, body: ICreateBpBudgetRequest): Observable<ApiResponse<IBpBudgetResponse>> {
    return this.wrap('createBudget', () => this.api.createBudget(planId, body), body);
  }

  updateBudget(
    planId: number,
    budgetId: number,
    body: IUpdateBpBudgetRequest,
  ): Observable<ApiResponse<IBpBudgetResponse>> {
    return this.wrap('updateBudget', () => this.api.updateBudget(planId, budgetId, body), body);
  }

  deleteBudget(planId: number, budgetId: number): Observable<ApiResponse<unknown>> {
    return this.wrap('deleteBudget', () => this.api.deleteBudget(planId, budgetId), { planId, budgetId });
  }

  createFundingLine(
    planId: number,
    budgetId: number,
    body: ICreateBpFundingLineRequest,
  ): Observable<ApiResponse<IBpFundingLineResponse>> {
    return this.wrap('createFundingLine', () => this.api.createFundingLine(planId, budgetId, body), body);
  }

  updateFundingLine(
    planId: number,
    budgetId: number,
    lineId: number,
    body: IUpdateBpFundingLineRequest,
  ): Observable<ApiResponse<IBpFundingLineResponse>> {
    return this.wrap('updateFundingLine', () => this.api.updateFundingLine(planId, budgetId, lineId, body), body);
  }

  deleteFundingLine(planId: number, budgetId: number, lineId: number): Observable<ApiResponse<unknown>> {
    return this.wrap('deleteFundingLine', () => this.api.deleteFundingLine(planId, budgetId, lineId), {
      planId,
      budgetId,
      lineId,
    });
  }

  private wrap<T>(
    shortName: string,
    call: () => Observable<ApiResponse<T>>,
    context: unknown,
  ): Observable<ApiResponse<T>> {
    const op = `BudgetPlanningApplicationService.${shortName}`;
    const t0 = Date.now();
    this.logger.debug(`Starting ${op}`, context);
    return call().pipe(
      tap((r) => this.logger.debug(`Ending ${op} (${Date.now() - t0}ms)`, r.statusMessage)),
      catchError((e) => {
        this.logger.error(`Failed ${op} (${Date.now() - t0}ms)`, e, context);
        return throwError(() => e);
      }),
    );
  }
}
