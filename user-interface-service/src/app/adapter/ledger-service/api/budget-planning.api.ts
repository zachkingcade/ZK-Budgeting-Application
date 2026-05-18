import { Injectable } from '@angular/core';
import { Observable, catchError, filter, tap, throwError } from 'rxjs';
import { LedgerHttpClientService } from '../client/ledger-http-client.service';
import { LedgerAdapterLoggerService } from '../logging/ledger-adapter-logger.service';
import { ApiResponse } from '../dto/ApiResponse';
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
} from '../dto/budget-planning/budget-planning.dto';

@Injectable({
  providedIn: 'root',
})
export class BudgetPlanningApi {
  constructor(
    private readonly ledgerClient: LedgerHttpClientService,
    private readonly logger: LedgerAdapterLoggerService,
  ) {}

  listPlans(): Observable<ApiResponse<IBudgetPlanListItem[]>> {
    const operation = 'BudgetPlanningApi.listPlans GET /budget-plans';
    const start = Date.now();
    this.logger.debug(`Starting ${operation}`);
    return this.ledgerClient.get<ApiResponse<IBudgetPlanListItem[]>>('/budget-plans').pipe(
      tap((r) => this.logger.debug(`Ending ${operation} (${Date.now() - start}ms)`, r.statusMessage)),
      catchError((err) => {
        this.logger.error(`Failed ${operation} (${Date.now() - start}ms)`, err);
        return throwError(() => err);
      }),
    );
  }

  createPlan(body: ICreateBudgetPlanRequest): Observable<ApiResponse<IBudgetPlanListItem>> {
    const operation = 'BudgetPlanningApi.createPlan POST /budget-plans';
    const start = Date.now();
    return this.ledgerClient.post<ApiResponse<IBudgetPlanListItem>>('/budget-plans', body).pipe(
      tap((r) => this.logger.debug(`Ending ${operation} (${Date.now() - start}ms)`, r.statusMessage)),
      catchError((err) => {
        this.logger.error(`Failed ${operation} (${Date.now() - start}ms)`, err, body);
        return throwError(() => err);
      }),
    );
  }

  duplicatePlan(
    planId: number,
    body: IDuplicateBudgetPlanRequest,
  ): Observable<ApiResponse<IBudgetPlanListItem>> {
    const path = `/budget-plans/${planId}/duplicate`;
    const operation = `BudgetPlanningApi.duplicatePlan POST ${path}`;
    const start = Date.now();
    return this.ledgerClient.post<ApiResponse<IBudgetPlanListItem>>(path, body).pipe(
      tap((r) => this.logger.debug(`Ending ${operation} (${Date.now() - start}ms)`, r.statusMessage)),
      catchError((err) => {
        this.logger.error(`Failed ${operation} (${Date.now() - start}ms)`, err, body);
        return throwError(() => err);
      }),
    );
  }

  renamePlan(planId: number, body: IRenameBudgetPlanRequest): Observable<ApiResponse<IBudgetPlanListItem>> {
    const path = `/budget-plans/${planId}`;
    const operation = `BudgetPlanningApi.renamePlan PATCH ${path}`;
    const start = Date.now();
    return this.ledgerClient.patch<ApiResponse<IBudgetPlanListItem>>(path, body).pipe(
      tap((r) => this.logger.debug(`Ending ${operation} (${Date.now() - start}ms)`, r.statusMessage)),
      catchError((err) => {
        this.logger.error(`Failed ${operation} (${Date.now() - start}ms)`, err, body);
        return throwError(() => err);
      }),
    );
  }

  deletePlan(planId: number): Observable<ApiResponse<unknown>> {
    const path = `/budget-plans/${planId}`;
    const operation = `BudgetPlanningApi.deletePlan DELETE ${path}`;
    const start = Date.now();
    return this.ledgerClient.delete<ApiResponse<unknown> | null>(path).pipe(
      filter((r): r is ApiResponse<unknown> => r != null),
      tap((r) => this.logger.debug(`Ending ${operation} (${Date.now() - start}ms)`, r.statusMessage)),
      catchError((err) => {
        this.logger.error(`Failed ${operation} (${Date.now() - start}ms)`, err);
        return throwError(() => err);
      }),
    );
  }

  loadEditor(planId: number): Observable<ApiResponse<IBudgetPlanEditorView>> {
    const path = `/budget-plans/${planId}/editor`;
    const operation = `BudgetPlanningApi.loadEditor GET ${path}`;
    const start = Date.now();
    return this.ledgerClient.get<ApiResponse<IBudgetPlanEditorView>>(path).pipe(
      tap((r) => this.logger.debug(`Ending ${operation} (${Date.now() - start}ms)`, r.statusMessage)),
      catchError((err) => {
        this.logger.error(`Failed ${operation} (${Date.now() - start}ms)`, err);
        return throwError(() => err);
      }),
    );
  }

  listIncomes(planId: number): Observable<ApiResponse<IBpIncomeResponse[]>> {
    const path = `/budget-plans/${planId}/incomes`;
    return this.ledgerClient.get<ApiResponse<IBpIncomeResponse[]>>(path).pipe(
      catchError((err) => throwError(() => err)),
    );
  }

  createIncome(planId: number, body: ICreateBpIncomeRequest): Observable<ApiResponse<IBpIncomeResponse>> {
    return this.ledgerClient
      .post<ApiResponse<IBpIncomeResponse>>(`/budget-plans/${planId}/incomes`, body)
      .pipe(catchError((err) => throwError(() => err)));
  }

  updateIncome(
    planId: number,
    incomeId: number,
    body: IUpdateBpIncomeRequest,
  ): Observable<ApiResponse<IBpIncomeResponse>> {
    return this.ledgerClient
      .patch<ApiResponse<IBpIncomeResponse>>(`/budget-plans/${planId}/incomes/${incomeId}`, body)
      .pipe(catchError((err) => throwError(() => err)));
  }

  deleteIncome(planId: number, incomeId: number): Observable<ApiResponse<unknown>> {
    return this.ledgerClient
      .delete<ApiResponse<unknown> | null>(`/budget-plans/${planId}/incomes/${incomeId}`)
      .pipe(
        filter((r): r is ApiResponse<unknown> => r != null),
        catchError((err) => throwError(() => err)),
      );
  }

  listRecurringPayables(planId: number): Observable<ApiResponse<IBpRecurringPayableResponse[]>> {
    return this.ledgerClient
      .get<ApiResponse<IBpRecurringPayableResponse[]>>(`/budget-plans/${planId}/recurring-payables`)
      .pipe(catchError((err) => throwError(() => err)));
  }

  createRecurringPayable(
    planId: number,
    body: ICreateBpRecurringPayableRequest,
  ): Observable<ApiResponse<IBpRecurringPayableResponse>> {
    return this.ledgerClient
      .post<ApiResponse<IBpRecurringPayableResponse>>(`/budget-plans/${planId}/recurring-payables`, body)
      .pipe(catchError((err) => throwError(() => err)));
  }

  updateRecurringPayable(
    planId: number,
    payableId: number,
    body: IUpdateBpRecurringPayableRequest,
  ): Observable<ApiResponse<IBpRecurringPayableResponse>> {
    return this.ledgerClient
      .patch<ApiResponse<IBpRecurringPayableResponse>>(
        `/budget-plans/${planId}/recurring-payables/${payableId}`,
        body,
      )
      .pipe(catchError((err) => throwError(() => err)));
  }

  deleteRecurringPayable(planId: number, payableId: number): Observable<ApiResponse<unknown>> {
    return this.ledgerClient
      .delete<ApiResponse<unknown> | null>(`/budget-plans/${planId}/recurring-payables/${payableId}`)
      .pipe(
        filter((r): r is ApiResponse<unknown> => r != null),
        catchError((err) => throwError(() => err)),
      );
  }

  listBudgets(planId: number): Observable<ApiResponse<IBpBudgetResponse[]>> {
    return this.ledgerClient
      .get<ApiResponse<IBpBudgetResponse[]>>(`/budget-plans/${planId}/budgets`)
      .pipe(catchError((err) => throwError(() => err)));
  }

  createBudget(planId: number, body: ICreateBpBudgetRequest): Observable<ApiResponse<IBpBudgetResponse>> {
    return this.ledgerClient
      .post<ApiResponse<IBpBudgetResponse>>(`/budget-plans/${planId}/budgets`, body)
      .pipe(catchError((err) => throwError(() => err)));
  }

  updateBudget(
    planId: number,
    budgetId: number,
    body: IUpdateBpBudgetRequest,
  ): Observable<ApiResponse<IBpBudgetResponse>> {
    return this.ledgerClient
      .put<ApiResponse<IBpBudgetResponse>>(`/budget-plans/${planId}/budgets/${budgetId}`, body)
      .pipe(catchError((err) => throwError(() => err)));
  }

  deleteBudget(planId: number, budgetId: number): Observable<ApiResponse<unknown>> {
    return this.ledgerClient
      .delete<ApiResponse<unknown> | null>(`/budget-plans/${planId}/budgets/${budgetId}`)
      .pipe(
        filter((r): r is ApiResponse<unknown> => r != null),
        catchError((err) => throwError(() => err)),
      );
  }

  listFundingLines(planId: number, budgetId: number): Observable<ApiResponse<IBpFundingLineResponse[]>> {
    return this.ledgerClient
      .get<ApiResponse<IBpFundingLineResponse[]>>(`/budget-plans/${planId}/budgets/${budgetId}/funding-lines`)
      .pipe(catchError((err) => throwError(() => err)));
  }

  createFundingLine(
    planId: number,
    budgetId: number,
    body: ICreateBpFundingLineRequest,
  ): Observable<ApiResponse<IBpFundingLineResponse>> {
    return this.ledgerClient
      .post<ApiResponse<IBpFundingLineResponse>>(
        `/budget-plans/${planId}/budgets/${budgetId}/funding-lines`,
        body,
      )
      .pipe(catchError((err) => throwError(() => err)));
  }

  updateFundingLine(
    planId: number,
    budgetId: number,
    lineId: number,
    body: IUpdateBpFundingLineRequest,
  ): Observable<ApiResponse<IBpFundingLineResponse>> {
    return this.ledgerClient
      .patch<ApiResponse<IBpFundingLineResponse>>(
        `/budget-plans/${planId}/budgets/${budgetId}/funding-lines/${lineId}`,
        body,
      )
      .pipe(catchError((err) => throwError(() => err)));
  }

  deleteFundingLine(planId: number, budgetId: number, lineId: number): Observable<ApiResponse<unknown>> {
    return this.ledgerClient
      .delete<ApiResponse<unknown> | null>(
        `/budget-plans/${planId}/budgets/${budgetId}/funding-lines/${lineId}`,
      )
      .pipe(
        filter((r): r is ApiResponse<unknown> => r != null),
        catchError((err) => throwError(() => err)),
      );
  }
}
