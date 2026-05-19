import { CommonModule } from '@angular/common';
import { Component, DestroyRef, OnInit, computed, signal } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { FormsModule } from '@angular/forms';
import { forkJoin } from 'rxjs';
import { map } from 'rxjs/operators';
import { MatButtonModule } from '@angular/material/button';
import { MatDialog } from '@angular/material/dialog';
import { MatCheckboxModule } from '@angular/material/checkbox';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatSelectModule } from '@angular/material/select';
import { MatTableModule } from '@angular/material/table';
import { MatTabsModule } from '@angular/material/tabs';
import { MatTooltipModule } from '@angular/material/tooltip';
import { AccountEnrichedObject } from '../../../adapter/ledger-service/dto/account/AccountEnrichedObject';
import {
  IBudgetPlanEditorView,
  IBudgetPlanListItem,
  IBpBudgetWithFundingResponse,
  IBpFundingLineResponse,
  IBpIncomeResponse,
  IBpRecurringPayableResponse,
  IIncomeFundingSummaryView,
} from '../../../adapter/ledger-service/dto/budget-planning/budget-planning.dto';
import { readLedgerApiErrorMessage } from '../../../adapter/ledger-service/ledger-http-error.util';
import { AccountsApplicationService } from '../../../application/ledger/accounts.application-service';
import { BudgetPlanningApplicationService } from '../../../application/planning/budget-planning.application-service';
import {
  formatIncomePayPeriodLabel,
  fundingPerIncomePayPeriodMinor,
} from '../../../domain/budget-planning/budget-plan-frequency';
import {
  dollarsStringToMinorUnits,
  formatMoneyDollarsOnBlur,
  minorUnitsToDollarsString,
  onMoneyDollarsKeydown,
  sanitizeMoneyDollarsInput,
} from '../../../domain/journal-entry/journal-entry.validation';
import { ToastService } from '../../../application/toast.service';
import {
  SaveAsReplayableDialogComponent,
  SaveAsReplayableDialogData,
} from '../../ledger/pending/save-as-replayable-dialog.component';
import { incomeFundingAllocationsToReplayableDebitDrafts } from '../../../domain/replayable-journal-entry/replayable-journal-entry.mapper';
import { PageCage } from '../../page-cage/page-cage.component';
import { ConfirmationModalComponent } from '../../shared/confirmation-modal/confirmation-modal.component';
import { TableRowActionsMenuComponent } from '../../shared/table-row-actions-menu/table-row-actions-menu.component';
import { ITableRowAction } from '../../shared/table-row-actions-menu/table-row-action.types';

const FREQUENCY_UNITS: readonly string[] = ['days', 'weeks', 'months', 'years'];

/** Matches ledger `BudgetPlanningService` allowed types: Budget, Fund, Savings, Investment. */
const BUDGET_PLAN_POOL_TYPE_NAMES = new Set(['Budget', 'Fund', 'Savings', 'Investment']);

/** Slice for overview pie charts (monthly amounts, 30-day basis). */
interface IBudgetOverviewPieSlice {
  label: string;
  amountMinor: number;
  percent: number;
  color: string;
}

type IBudgetOverviewPieSliceWithPath = IBudgetOverviewPieSlice & { pathD: string };

@Component({
  selector: 'app-budget-planning-page',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule,
    PageCage,
    MatButtonModule,
    MatIconModule,
    MatTableModule,
    MatTabsModule,
    MatFormFieldModule,
    MatInputModule,
    MatSelectModule,
    MatProgressSpinnerModule,
    MatCheckboxModule,
    MatTooltipModule,
    ConfirmationModalComponent,
    TableRowActionsMenuComponent,
  ],
  templateUrl: './budget-planning-page.component.html',
  styleUrl: './budget-planning-page.component.scss',
})
export class BudgetPlanningPageComponent implements OnInit {
  constructor(
    private readonly budgetPlanningApplicationService: BudgetPlanningApplicationService,
    private readonly accountsApplicationService: AccountsApplicationService,
    private readonly dialog: MatDialog,
    private readonly toast: ToastService,
    private readonly destroyRef: DestroyRef,
  ) {}

  readonly frequencyUnits = FREQUENCY_UNITS;

  readonly plans = signal<IBudgetPlanListItem[]>([]);
  readonly editor = signal<IBudgetPlanEditorView | null>(null);
  readonly activePlanId = signal<number | null>(null);
  readonly accounts = signal<AccountEnrichedObject[]>([]);

  readonly listLoading = signal(false);
  readonly listError = signal<string | null>(null);
  readonly editorLoading = signal(false);
  readonly editorError = signal<string | null>(null);
  readonly actionError = signal<string | null>(null);

  readonly deletePlanTarget = signal<IBudgetPlanListItem | null>(null);
  readonly addPlanOpen = signal(false);
  readonly newPlanName = signal('');

  readonly renameTarget = signal<IBudgetPlanListItem | null>(null);
  readonly renameDraft = signal('');

  readonly duplicateTarget = signal<IBudgetPlanListItem | null>(null);
  readonly duplicateName = signal('');
  readonly duplicateAdvancedOpen = signal(false);
  readonly duplicateCopyIncomes = signal(true);
  readonly duplicateCopyRecurringPayables = signal(true);
  readonly duplicateCopyBudgetAmounts = signal(true);
  readonly duplicateError = signal<string | null>(null);
  readonly duplicateSubmitting = signal(false);

  readonly expandedBudgetIds = signal<Set<number>>(new Set());
  readonly expandedIncomeIds = signal<Set<number>>(new Set());

  readonly incomeForm = signal<IIncomeFormState | null>(null);
  readonly payableForm = signal<IPayableFormState | null>(null);
  readonly budgetForm = signal<IBudgetFormState | null>(null);
  readonly fundingForm = signal<IFundingFormState | null>(null);

  readonly planColumns = ['name', 'createdAt', 'lastEdited', 'actions'] as const;
  readonly payableColumns = [
    'description',
    'account',
    'amount',
    'frequency',
    'perDay',
    'perWeek',
    'perMonth',
    'perYear',
    'actions',
  ] as const;
  readonly summaryColumns = [
    'account',
    'incDay',
    'incWeek',
    'incMonth',
    'incYear',
    'remainingBar',
    'allocDay',
    'allocWeek',
    'allocMonth',
    'allocYear',
    'remWeek',
    'remMonth',
    'remYear',
  ] as const;
  readonly fundingColumns = ['income', 'amount', 'frequency', 'perDay', 'actions'] as const;

  readonly overviewIncomeSlices = computed(() => this.buildOverviewIncomeSlices());
  readonly overviewBudgetedSlices = computed(() => this.buildOverviewBudgetedSlices());
  readonly overviewTypeSlices = computed(() => this.buildOverviewTypeSlices());

  readonly overviewIncomePaths = computed(() => this.piePathsFromSlices(this.overviewIncomeSlices()));
  readonly overviewBudgetedPaths = computed(() => this.piePathsFromSlices(this.overviewBudgetedSlices()));
  readonly overviewTypePaths = computed(() => this.piePathsFromSlices(this.overviewTypeSlices()));

  ngOnInit(): void {
    this.refreshPlanList();
  }

  formatMoney(minorUnits: number): string {
    const major = minorUnits / 100;
    return new Intl.NumberFormat(undefined, {
      style: 'currency',
      currency: 'USD',
      minimumFractionDigits: 2,
      maximumFractionDigits: 2,
    }).format(major);
  }

  overviewPieTooltip(s: IBudgetOverviewPieSlice): string {
    return `${s.label}: ${this.formatMoney(s.amountMinor)} (${s.percent.toFixed(1)}%)`;
  }

  overviewTotalMinor(slices: IBudgetOverviewPieSlice[]): number {
    return slices.reduce((sum, s) => sum + s.amountMinor, 0);
  }

  private buildOverviewIncomeSlices(): IBudgetOverviewPieSlice[] {
    const ed = this.editor();
    if (!ed) {
      return [];
    }
    const incomeMap = new Map<string, number>();
    for (const row of ed.incomes) {
      const label = row.accountDescription || `Income #${row.bpIncomeId}`;
      incomeMap.set(label, (incomeMap.get(label) ?? 0) + row.amountPerMonthMinor);
    }
    return this.buildOverviewSlicesFromMap(incomeMap, 210);
  }

  private buildOverviewBudgetedSlices(): IBudgetOverviewPieSlice[] {
    const ed = this.editor();
    if (!ed) {
      return [];
    }
    const budgetedMap = new Map<string, number>();
    for (const b of ed.budgets) {
      const label = b.budget.accountDescription || `Budget #${b.budget.bpBudgetId}`;
      const fundedMonth = b.fundingLines.reduce((s, line) => s + line.amountPerMonthMinor, 0);
      budgetedMap.set(label, (budgetedMap.get(label) ?? 0) + fundedMonth);
    }
    return this.buildOverviewSlicesFromMap(budgetedMap, 130);
  }

  private buildOverviewTypeSlices(): IBudgetOverviewPieSlice[] {
    const ed = this.editor();
    const accountRows = this.accounts();
    if (!ed) {
      return [];
    }
    const byAccountId = new Map(accountRows.map((a) => [a.accountId, a]));
    const typeMap = new Map<string, number>();
    for (const b of ed.budgets) {
      const acc = byAccountId.get(b.budget.accountId);
      const typeLabel = acc?.accountTypeName ?? 'Unknown type';
      const fundedMonth = b.fundingLines.reduce((s, line) => s + line.amountPerMonthMinor, 0);
      typeMap.set(typeLabel, (typeMap.get(typeLabel) ?? 0) + fundedMonth);
    }
    return this.buildOverviewSlicesFromMap(typeMap, 30);
  }

  private buildOverviewSlicesFromMap(
    amountsByLabel: Map<string, number>,
    hueStart: number,
  ): IBudgetOverviewPieSlice[] {
    const entries = [...amountsByLabel.entries()].filter(([, v]) => v > 0);
    const total = entries.reduce((s, [, v]) => s + v, 0);
    if (total <= 0) {
      return [];
    }
    return entries.map(([label, amount], i) => ({
      label,
      amountMinor: amount,
      percent: Math.round((amount / total) * 1000) / 10,
      color: `hsl(${(hueStart + i * 47) % 360} 58% 46%)`,
    }));
  }

  /** SVG path wedges for pie hover (MatTooltip on each path). */
  piePathsFromSlices(slices: IBudgetOverviewPieSlice[]): IBudgetOverviewPieSliceWithPath[] {
    const cx = 100;
    const cy = 100;
    const r = 92;
    const total = slices.reduce((s, x) => s + x.amountMinor, 0);
    if (total <= 0) {
      return [];
    }
    let angle = -Math.PI / 2;
    return slices.map((slice) => {
      const span = (slice.amountMinor / total) * 2 * Math.PI;
      const a0 = angle;
      const a1 = angle + span;
      angle = a1;
      return { ...slice, pathD: this.svgPieWedge(cx, cy, r, a0, a1) };
    });
  }

  private svgPieWedge(cx: number, cy: number, r: number, a0: number, a1: number): string {
    const x0 = cx + r * Math.cos(a0);
    const y0 = cy + r * Math.sin(a0);
    const x1 = cx + r * Math.cos(a1);
    const y1 = cy + r * Math.sin(a1);
    const largeArc = a1 - a0 > Math.PI ? 1 : 0;
    return `M ${cx} ${cy} L ${x0} ${y0} A ${r} ${r} 0 ${largeArc} 1 ${x1} ${y1} Z`;
  }

  /** 0–1: share of per-day income still unallocated to budgets (capped). */
  incomeRemainingProgress(summary: IIncomeFundingSummaryView): number {
    const denom = Math.abs(summary.incomeAmountPerDayMinor);
    if (denom === 0) {
      return 0;
    }
    const rem = summary.remainingPerDayMinor;
    return Math.min(1, Math.max(0, rem / denom));
  }

  incomeRemainingBarColor(summary: IIncomeFundingSummaryView): string {
    const p = this.incomeRemainingProgress(summary);
    const hue = Math.round(p * 120);
    return `hsl(${hue} 72% 40%)`;
  }

  /** Recurring payables whose budget account has no budget row on this plan yet. */
  recurringPayablesWithoutBudgetOnPlan(ed: IBudgetPlanEditorView): IBpRecurringPayableResponse[] {
    const budgetAccountIds = new Set(ed.budgets.map((b) => b.budget.accountId));
    return ed.recurringPayables.filter((p) => !budgetAccountIds.has(p.budgetAccountId));
  }

  refreshPlanList(): void {
    this.listLoading.set(true);
    this.listError.set(null);
    this.budgetPlanningApplicationService
      .listPlans()
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: (res) => {
          this.plans.set(res.data ?? []);
          this.listLoading.set(false);
        },
        error: (err: unknown) => {
          this.listError.set(readLedgerApiErrorMessage(err, 'Could not load budget plans.'));
          this.listLoading.set(false);
        },
      });
  }

  openAddPlan(): void {
    this.addPlanOpen.set(true);
    this.newPlanName.set('');
    this.actionError.set(null);
  }

  cancelAddPlan(): void {
    this.addPlanOpen.set(false);
  }

  submitAddPlan(): void {
    const name = this.newPlanName().trim();
    if (!name) {
      this.actionError.set('Plan name is required.');
      return;
    }
    this.actionError.set(null);
    this.budgetPlanningApplicationService
      .createPlan({ name })
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: () => {
          this.addPlanOpen.set(false);
          this.refreshPlanList();
        },
        error: (err: unknown) => {
          this.actionError.set(readLedgerApiErrorMessage(err, 'Could not create plan.'));
        },
      });
  }

  requestDeletePlan(plan: IBudgetPlanListItem): void {
    this.deletePlanTarget.set(plan);
  }

  confirmDeletePlan(): void {
    const plan = this.deletePlanTarget();
    if (!plan) {
      return;
    }
    this.budgetPlanningApplicationService
      .deletePlan(plan.budgetPlanId)
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: () => {
          this.deletePlanTarget.set(null);
          if (this.activePlanId() === plan.budgetPlanId) {
            this.unloadPlan();
          }
          this.refreshPlanList();
        },
        error: (err: unknown) => {
          this.actionError.set(readLedgerApiErrorMessage(err, 'Could not delete plan.'));
          this.deletePlanTarget.set(null);
        },
      });
  }

  cancelDeletePlan(): void {
    this.deletePlanTarget.set(null);
  }

  openRename(plan: IBudgetPlanListItem): void {
    this.renameTarget.set(plan);
    this.renameDraft.set(plan.name);
    this.actionError.set(null);
  }

  cancelRename(): void {
    this.renameTarget.set(null);
  }

  submitRename(): void {
    const plan = this.renameTarget();
    const name = this.renameDraft().trim();
    if (!plan || !name) {
      this.actionError.set('Name is required.');
      return;
    }
    this.budgetPlanningApplicationService
      .renamePlan(plan.budgetPlanId, { name })
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: () => {
          this.renameTarget.set(null);
          this.refreshPlanList();
          const ed = this.editor();
          if (ed && ed.plan.budgetPlanId === plan.budgetPlanId) {
            void this.reloadEditor(plan.budgetPlanId);
          }
        },
        error: (err: unknown) => {
          this.actionError.set(readLedgerApiErrorMessage(err, 'Could not rename plan.'));
        },
      });
  }

  openPlan(planId: number): void {
    this.editorLoading.set(true);
    this.editorError.set(null);
    this.actionError.set(null);
    this.activePlanId.set(planId);
    forkJoin({
      editor: this.budgetPlanningApplicationService.loadEditor(planId).pipe(map((r) => r.data)),
      accounts: this.accountsApplicationService.getAll().pipe(map((r) => r.data.accountsList ?? [])),
    })
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: ({ editor, accounts }) => {
          this.editor.set(editor);
          this.accounts.set(accounts);
          this.editorLoading.set(false);
        },
        error: (err: unknown) => {
          this.editorError.set(readLedgerApiErrorMessage(err, 'Could not open plan.'));
          this.editorLoading.set(false);
          this.activePlanId.set(null);
          this.editor.set(null);
        },
      });
  }

  unloadPlan(): void {
    this.editor.set(null);
    this.activePlanId.set(null);
    this.expandedBudgetIds.set(new Set());
    this.expandedIncomeIds.set(new Set());
    this.closeAllForms();
    this.refreshPlanList();
  }

  reloadEditor(planId: number): void {
    this.budgetPlanningApplicationService
      .loadEditor(planId)
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: (res) => {
          this.editor.set(res.data);
        },
        error: (err: unknown) => {
          this.editorError.set(readLedgerApiErrorMessage(err, 'Could not refresh plan.'));
        },
      });
  }

  revenueAccountOptions(): { id: number; label: string }[] {
    return this.accounts()
      .filter((a) => a.accountTypeName === 'Income')
      .map((a) => ({ id: a.accountId, label: a.accountDisplayName }));
  }

  /** Budget / Fund / Savings / Investment accounts (plan budgets and recurring payables). */
  budgetAccountOptions(): { id: number; label: string }[] {
    return this.accounts()
      .filter((a) => BUDGET_PLAN_POOL_TYPE_NAMES.has(a.accountTypeName))
      .map((a) => ({ id: a.accountId, label: a.accountDisplayName }))
      .sort((x, y) => x.label.localeCompare(y.label));
  }

  /** Budget pool accounts not already used by another budget row in this plan (editing row keeps its account selectable). */
  budgetAccountOptionsForBudgetForm(): { id: number; label: string }[] {
    const ed = this.editor();
    const st = this.budgetForm();
    const usedOther = new Set<number>();
    if (ed) {
      for (const row of ed.budgets) {
        if (st?.editingId != null && row.budget.bpBudgetId === st.editingId) {
          continue;
        }
        usedOther.add(row.budget.accountId);
      }
    }
    return this.accounts()
      .filter((a) => BUDGET_PLAN_POOL_TYPE_NAMES.has(a.accountTypeName) && !usedOther.has(a.accountId))
      .map((a) => ({ id: a.accountId, label: a.accountDisplayName }))
      .sort((x, y) => x.label.localeCompare(y.label));
  }

  budgetFundedPerDayMinor(row: IBpBudgetWithFundingResponse): number {
    return row.fundingLines.reduce((sum, line) => sum + line.amountPerDayMinor, 0);
  }

  budgetFundedPerWeekMinor(row: IBpBudgetWithFundingResponse): number {
    return row.fundingLines.reduce((sum, line) => sum + line.amountPerWeekMinor, 0);
  }

  budgetFundedPerMonthMinor(row: IBpBudgetWithFundingResponse): number {
    return row.fundingLines.reduce((sum, line) => sum + line.amountPerMonthMinor, 0);
  }

  budgetFundedPerYearMinor(row: IBpBudgetWithFundingResponse): number {
    return row.fundingLines.reduce((sum, line) => sum + line.amountPerYearMinor, 0);
  }

  recurringPayablesForBudgetAccount(
    b: IBpBudgetWithFundingResponse,
    ed: IBudgetPlanEditorView,
  ): IBpRecurringPayableResponse[] {
    const aid = b.budget.accountId;
    return ed.recurringPayables.filter((p) => p.budgetAccountId === aid);
  }

  recurringPayablesPerDaySumMinor(b: IBpBudgetWithFundingResponse, ed: IBudgetPlanEditorView): number {
    return this.recurringPayablesForBudgetAccount(b, ed).reduce((s, p) => s + p.amountPerDayMinor, 0);
  }

  recurringPayablesTotalsForBudget(
    b: IBpBudgetWithFundingResponse,
    ed: IBudgetPlanEditorView,
  ): {
    amountMinor: number;
    perDay: number;
    perWeek: number;
    perMonth: number;
    perYear: number;
  } {
    const rows = this.recurringPayablesForBudgetAccount(b, ed);
    return {
      amountMinor: rows.reduce((s, p) => s + p.amountMinor, 0),
      perDay: rows.reduce((s, p) => s + p.amountPerDayMinor, 0),
      perWeek: rows.reduce((s, p) => s + p.amountPerWeekMinor, 0),
      perMonth: rows.reduce((s, p) => s + p.amountPerMonthMinor, 0),
      perYear: rows.reduce((s, p) => s + p.amountPerYearMinor, 0),
    };
  }

  /** Combined per-day rate of recurring payables for an account (UI-only; not stored on the budget row). */
  recurringPayablesRequiredPerDayMinor(accountId: number | null, ed: IBudgetPlanEditorView): number {
    if (accountId == null) {
      return 0;
    }
    return ed.recurringPayables
      .filter((p) => p.budgetAccountId === accountId)
      .reduce((s, p) => s + p.amountPerDayMinor, 0);
  }

  /** True when budgeted (funded) per day meets or exceeds required recurring per day. */
  budgetRequiredMetPerDay(b: IBpBudgetWithFundingResponse, ed: IBudgetPlanEditorView): boolean {
    const need = this.recurringPayablesPerDaySumMinor(b, ed);
    if (need <= 0) {
      return true;
    }
    return this.budgetFundedPerDayMinor(b) + 1e-9 >= need;
  }

  budgetRequiredRowClass(b: IBpBudgetWithFundingResponse, ed: IBudgetPlanEditorView): string {
    const need = this.recurringPayablesPerDaySumMinor(b, ed);
    if (need <= 0) {
      return 'bp__budgetSummaryReq--neutral';
    }
    return this.budgetRequiredMetPerDay(b, ed) ? 'bp__budgetSummaryReq--met' : 'bp__budgetSummaryReq--short';
  }

  /** 0–1: budgeted vs required per day; full when nothing is required. */
  budgetFundedProgress(b: IBpBudgetWithFundingResponse, ed: IBudgetPlanEditorView): number {
    const required = this.recurringPayablesPerDaySumMinor(b, ed);
    if (required <= 0) {
      return 1;
    }
    const funded = this.budgetFundedPerDayMinor(b);
    return Math.min(1, Math.max(0, funded / required));
  }

  budgetFundedBarColor(b: IBpBudgetWithFundingResponse, ed: IBudgetPlanEditorView): string {
    return this.budgetRequiredMetPerDay(b, ed) ? 'hsl(130 52% 34%)' : 'hsl(0 62% 42%)';
  }

  incomeOptionsForFunding(): { id: number; label: string }[] {
    return this.editor()?.incomes.map((i) => ({ id: i.bpIncomeId, label: i.accountDescription })) ?? [];
  }

  buildPlanRowActions(_plan: IBudgetPlanListItem): ITableRowAction[] {
    return [
      { id: 'open', label: 'Open', icon: 'folder_open' },
      { id: 'duplicate', label: 'Duplicate', icon: 'content_copy' },
      { id: 'rename', label: 'Rename', icon: 'edit' },
      { id: 'delete', label: 'Delete', icon: 'delete' },
    ];
  }

  onPlanRowAction(sel: { id: string }, plan: IBudgetPlanListItem): void {
    switch (sel.id) {
      case 'open':
        this.openPlan(plan.budgetPlanId);
        break;
      case 'duplicate':
        this.openDuplicate(plan);
        break;
      case 'rename':
        this.openRename(plan);
        break;
      case 'delete':
        this.requestDeletePlan(plan);
        break;
      default:
        break;
    }
  }

  buildIncomeRowActions(row: IBpIncomeResponse): ITableRowAction[] {
    const ed = this.editor();
    const hasFunding =
      ed != null && this.fundingAllocationsForIncome(row, ed).length > 0;
    return [
      {
        id: 'saveReplayable',
        label: 'Save as replayable JE',
        icon: 'bookmark_add',
        disabled: !hasFunding,
      },
      { id: 'edit', label: 'Edit', icon: 'edit' },
      { id: 'delete', label: 'Delete', icon: 'delete' },
    ];
  }

  onIncomeRowAction(sel: { id: string }, row: IBpIncomeResponse): void {
    switch (sel.id) {
      case 'saveReplayable':
        this.openSaveIncomeAsReplayable(row);
        break;
      case 'edit':
        this.openEditIncome(row);
        break;
      case 'delete':
        this.deleteIncome(row);
        break;
      default:
        break;
    }
  }

  buildPayableRowActions(_row: IBpRecurringPayableResponse): ITableRowAction[] {
    return [
      { id: 'edit', label: 'Edit', icon: 'edit' },
      { id: 'delete', label: 'Delete', icon: 'delete' },
    ];
  }

  onPayableRowAction(sel: { id: string }, row: IBpRecurringPayableResponse): void {
    switch (sel.id) {
      case 'edit':
        this.openEditPayable(row);
        break;
      case 'delete':
        this.deletePayable(row);
        break;
      default:
        break;
    }
  }

  buildBudgetRowActions(_b: IBpBudgetWithFundingResponse): ITableRowAction[] {
    return [
      { id: 'edit', label: 'Edit', icon: 'edit' },
      { id: 'delete', label: 'Delete', icon: 'delete' },
    ];
  }

  onBudgetRowAction(sel: { id: string }, b: IBpBudgetWithFundingResponse): void {
    switch (sel.id) {
      case 'edit':
        this.openEditBudget(b);
        break;
      case 'delete':
        this.deleteBudget(b);
        break;
      default:
        break;
    }
  }

  buildFundingRowActions(_line: IBpFundingLineResponse): ITableRowAction[] {
    return [
      { id: 'edit', label: 'Edit', icon: 'edit' },
      { id: 'delete', label: 'Delete', icon: 'delete' },
    ];
  }

  onFundingRowAction(sel: { id: string }, budgetId: number, line: IBpFundingLineResponse): void {
    switch (sel.id) {
      case 'edit':
        this.openEditFunding(budgetId, line);
        break;
      case 'delete':
        this.deleteFunding(budgetId, line);
        break;
      default:
        break;
    }
  }

  openDuplicate(plan: IBudgetPlanListItem): void {
    this.duplicateTarget.set(plan);
    this.duplicateName.set(`Copy of ${plan.name}`);
    this.duplicateAdvancedOpen.set(false);
    this.duplicateCopyIncomes.set(true);
    this.duplicateCopyRecurringPayables.set(true);
    this.duplicateCopyBudgetAmounts.set(true);
    this.duplicateError.set(null);
    this.duplicateSubmitting.set(false);
  }

  cancelDuplicate(): void {
    if (this.duplicateSubmitting()) {
      return;
    }
    this.duplicateTarget.set(null);
    this.duplicateError.set(null);
  }

  toggleDuplicateAdvanced(): void {
    this.duplicateAdvancedOpen.update((v) => !v);
  }

  submitDuplicate(): void {
    const plan = this.duplicateTarget();
    const name = this.duplicateName().trim();
    if (!plan) {
      return;
    }
    if (!name) {
      this.duplicateError.set('Plan name is required.');
      return;
    }
    this.duplicateError.set(null);
    this.duplicateSubmitting.set(true);
    this.budgetPlanningApplicationService
      .duplicatePlan(plan.budgetPlanId, {
        name,
        copyIncomes: this.duplicateCopyIncomes(),
        copyRecurringPayables: this.duplicateCopyRecurringPayables(),
        copyBudgetAmounts: this.duplicateCopyBudgetAmounts(),
      })
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: (res) => {
          this.duplicateSubmitting.set(false);
          this.duplicateTarget.set(null);
          this.refreshPlanList();
          const created = res.data;
          if (created) {
            this.openPlan(created.budgetPlanId);
          }
        },
        error: (err: unknown) => {
          this.duplicateSubmitting.set(false);
          this.duplicateError.set(readLedgerApiErrorMessage(err, 'Could not duplicate plan.'));
        },
      });
  }

  toggleBudgetExpand(budgetId: number): void {
    this.expandedBudgetIds.update((prev) => {
      const n = new Set(prev);
      if (n.has(budgetId)) {
        n.delete(budgetId);
      } else {
        n.add(budgetId);
      }
      return n;
    });
  }

  budgetExpanded(id: number): boolean {
    return this.expandedBudgetIds().has(id);
  }

  toggleIncomeExpand(bpIncomeId: number): void {
    this.expandedIncomeIds.update((prev) => {
      const n = new Set(prev);
      if (n.has(bpIncomeId)) {
        n.delete(bpIncomeId);
      } else {
        n.add(bpIncomeId);
      }
      return n;
    });
  }

  incomeExpanded(bpIncomeId: number): boolean {
    return this.expandedIncomeIds().has(bpIncomeId);
  }

  incomePayPeriodLabel(income: IBpIncomeResponse): string {
    return formatIncomePayPeriodLabel(income.frequencyNumber, income.frequencyUnit);
  }

  fundingAllocationsForIncome(
    income: IBpIncomeResponse,
    ed: IBudgetPlanEditorView,
  ): IIncomeFundingAllocationView[] {
    const rows: IIncomeFundingAllocationView[] = [];
    for (const b of ed.budgets) {
      for (const line of b.fundingLines) {
        if (line.bpIncomeId !== income.bpIncomeId) {
          continue;
        }
        const perPayment = Math.round(
          fundingPerIncomePayPeriodMinor(
            line.amountPerDayMinor,
            income.frequencyUnit,
            income.frequencyNumber,
          ),
        );
        rows.push({
          accountId: b.budget.accountId,
          budgetAccountDescription: b.budget.accountDescription,
          amountPerIncomePayPeriodMinor: perPayment,
          fundingStatedAmountMinor: line.amountMinor,
          fundingFrequencyLabel: formatIncomePayPeriodLabel(line.frequencyNumber, line.frequencyUnit),
        });
      }
    }
    return rows.sort((a, b) => a.budgetAccountDescription.localeCompare(b.budgetAccountDescription));
  }

  totalAllocatedPerIncomePayPeriod(allocations: IIncomeFundingAllocationView[]): number {
    return allocations.reduce((s, a) => s + a.amountPerIncomePayPeriodMinor, 0);
  }

  openAddIncome(): void {
    this.incomeForm.set({
      editingId: null,
      amountDollars: '',
      frequencyUnit: 'months',
      frequencyNumber: 1,
      accountId: null,
      error: null,
    });
  }

  openEditIncome(row: IBpIncomeResponse): void {
    this.incomeForm.set({
      editingId: row.bpIncomeId,
      amountDollars: minorUnitsToDollarsString(row.amountMinor),
      frequencyUnit: row.frequencyUnit,
      frequencyNumber: row.frequencyNumber,
      accountId: row.accountId,
      error: null,
    });
  }

  cancelIncomeForm(): void {
    this.incomeForm.set(null);
  }

  submitIncomeForm(): void {
    const planId = this.activePlanId();
    const st = this.incomeForm();
    if (!planId || !st) {
      return;
    }
    const minor = dollarsStringToMinorUnits(st.amountDollars);
    if (minor == null || minor === 0) {
      this.incomeForm.set({ ...st, error: 'Enter a non-zero amount.' });
      return;
    }
    if (st.accountId == null) {
      this.incomeForm.set({ ...st, error: 'Select an account.' });
      return;
    }
    const body = {
      amountMinor: minor,
      frequencyUnit: st.frequencyUnit,
      frequencyNumber: st.frequencyNumber,
      accountId: st.accountId,
    };
    const req$ =
      st.editingId == null
        ? this.budgetPlanningApplicationService.createIncome(planId, body)
        : this.budgetPlanningApplicationService.updateIncome(planId, st.editingId, body);
    req$.pipe(takeUntilDestroyed(this.destroyRef)).subscribe({
      next: () => {
        this.incomeForm.set(null);
        this.reloadEditor(planId);
      },
      error: (err: unknown) => {
        this.incomeForm.set({
          ...st,
          error: readLedgerApiErrorMessage(err, 'Could not save income.'),
        });
      },
    });
  }

  openSaveIncomeAsReplayable(row: IBpIncomeResponse): void {
    const ed = this.editor();
    if (!ed) {
      return;
    }
    const allocations = this.fundingAllocationsForIncome(row, ed);
    if (allocations.length === 0) {
      this.toast.showError(
        'Add funding lines on budgets for this income before saving as a replayable entry.',
      );
      return;
    }
    const lines = incomeFundingAllocationsToReplayableDebitDrafts(allocations);
    if (lines.length === 0) {
      this.toast.showError('Funding amounts are all zero; nothing to save.');
      return;
    }
    const periodLabel = this.incomePayPeriodLabel(row);
    const data: SaveAsReplayableDialogData = {
      defaultName: `${row.accountDescription} – ${periodLabel}`,
      lines,
      requireBalanced: false,
      hint:
        `Saves one debit line per funded budget account (amount per ${periodLabel.toLowerCase()} payment from ${row.accountDescription}). ` +
        'Add matching credit line(s) when you load this replayable entry.',
    };
    this.dialog
      .open(SaveAsReplayableDialogComponent, { data, width: '32rem' })
      .afterClosed()
      .subscribe((saved) => {
        if (saved) {
          this.toast.showSuccess('Saved income as replayable journal entry.');
        }
      });
  }

  deleteIncome(row: IBpIncomeResponse): void {
    const planId = this.activePlanId();
    if (!planId) {
      return;
    }
    this.budgetPlanningApplicationService
      .deleteIncome(planId, row.bpIncomeId)
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: () => this.reloadEditor(planId),
        error: (err: unknown) => this.actionError.set(readLedgerApiErrorMessage(err, 'Could not delete income.')),
      });
  }

  openAddPayable(): void {
    this.payableForm.set({
      editingId: null,
      description: '',
      amountDollars: '',
      frequencyUnit: 'months',
      frequencyNumber: 1,
      budgetAccountId: null,
      error: null,
    });
  }

  openEditPayable(row: IBpRecurringPayableResponse): void {
    this.payableForm.set({
      editingId: row.bpRecurringPayableId,
      description: row.description,
      amountDollars: minorUnitsToDollarsString(row.amountMinor),
      frequencyUnit: row.frequencyUnit,
      frequencyNumber: row.frequencyNumber,
      budgetAccountId: row.budgetAccountId,
      error: null,
    });
  }

  cancelPayableForm(): void {
    this.payableForm.set(null);
  }

  submitPayableForm(): void {
    const planId = this.activePlanId();
    const st = this.payableForm();
    if (!planId || !st) {
      return;
    }
    const minor = dollarsStringToMinorUnits(st.amountDollars);
    if (minor == null || minor === 0) {
      this.payableForm.set({ ...st, error: 'Enter a non-zero amount.' });
      return;
    }
    if (!st.description.trim()) {
      this.payableForm.set({ ...st, error: 'Description is required.' });
      return;
    }
    if (st.budgetAccountId == null) {
      this.payableForm.set({ ...st, error: 'Select a budget account.' });
      return;
    }
    const body = {
      description: st.description.trim(),
      amountMinor: minor,
      frequencyUnit: st.frequencyUnit,
      frequencyNumber: st.frequencyNumber,
      budgetAccountId: st.budgetAccountId,
    };
    const req$ =
      st.editingId == null
        ? this.budgetPlanningApplicationService.createRecurringPayable(planId, body)
        : this.budgetPlanningApplicationService.updateRecurringPayable(planId, st.editingId, body);
    req$.pipe(takeUntilDestroyed(this.destroyRef)).subscribe({
      next: () => {
        this.payableForm.set(null);
        this.reloadEditor(planId);
      },
      error: (err: unknown) => {
        this.payableForm.set({
          ...st,
          error: readLedgerApiErrorMessage(err, 'Could not save recurring payable.'),
        });
      },
    });
  }

  deletePayable(row: IBpRecurringPayableResponse): void {
    const planId = this.activePlanId();
    if (!planId) {
      return;
    }
    this.budgetPlanningApplicationService
      .deleteRecurringPayable(planId, row.bpRecurringPayableId)
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: () => this.reloadEditor(planId),
        error: (err: unknown) =>
          this.actionError.set(readLedgerApiErrorMessage(err, 'Could not delete recurring payable.')),
      });
  }

  openAddBudget(): void {
    this.budgetForm.set({
      editingId: null,
      accountId: null,
      error: null,
    });
  }

  openEditBudget(row: IBpBudgetWithFundingResponse): void {
    const b = row.budget;
    this.budgetForm.set({
      editingId: b.bpBudgetId,
      accountId: b.accountId,
      error: null,
    });
  }

  cancelBudgetForm(): void {
    this.budgetForm.set(null);
  }

  submitBudgetForm(): void {
    const planId = this.activePlanId();
    const ed = this.editor();
    const st = this.budgetForm();
    if (!planId || !ed || !st) {
      return;
    }
    if (st.accountId == null) {
      this.budgetForm.set({ ...st, error: 'Select a budget account.' });
      return;
    }
    const requiredPerDay = this.recurringPayablesRequiredPerDayMinor(st.accountId, ed);
    const amountMinor = requiredPerDay > 0 ? Math.max(1, Math.ceil(requiredPerDay)) : 100;
    const body = {
      amountMinor,
      frequencyUnit: 'days',
      frequencyNumber: 1,
      accountId: st.accountId,
    };
    const req$ =
      st.editingId == null
        ? this.budgetPlanningApplicationService.createBudget(planId, body)
        : this.budgetPlanningApplicationService.updateBudget(planId, st.editingId, body);
    req$.pipe(takeUntilDestroyed(this.destroyRef)).subscribe({
      next: () => {
        this.budgetForm.set(null);
        this.reloadEditor(planId);
      },
      error: (err: unknown) => {
        this.budgetForm.set({
          ...st,
          error: readLedgerApiErrorMessage(err, 'Could not save budget.'),
        });
      },
    });
  }

  deleteBudget(row: IBpBudgetWithFundingResponse): void {
    const planId = this.activePlanId();
    if (!planId) {
      return;
    }
    this.budgetPlanningApplicationService
      .deleteBudget(planId, row.budget.bpBudgetId)
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: () => this.reloadEditor(planId),
        error: (err: unknown) => this.actionError.set(readLedgerApiErrorMessage(err, 'Could not delete budget.')),
      });
  }

  openAddFunding(budgetId: number): void {
    this.fundingForm.set({
      budgetId,
      editingId: null,
      bpIncomeId: null,
      amountDollars: '',
      frequencyUnit: 'months',
      frequencyNumber: 1,
      error: null,
    });
  }

  openEditFunding(budgetId: number, line: IBpFundingLineResponse): void {
    this.fundingForm.set({
      budgetId,
      editingId: line.bpFundingLineId,
      bpIncomeId: line.bpIncomeId,
      amountDollars: minorUnitsToDollarsString(line.amountMinor),
      frequencyUnit: line.frequencyUnit,
      frequencyNumber: line.frequencyNumber,
      error: null,
    });
  }

  cancelFundingForm(): void {
    this.fundingForm.set(null);
  }

  submitFundingForm(): void {
    const planId = this.activePlanId();
    const st = this.fundingForm();
    if (!planId || !st) {
      return;
    }
    const minor = dollarsStringToMinorUnits(st.amountDollars);
    if (minor == null || minor === 0) {
      this.fundingForm.set({ ...st, error: 'Enter a non-zero amount.' });
      return;
    }
    if (st.bpIncomeId == null) {
      this.fundingForm.set({ ...st, error: 'Select an income source.' });
      return;
    }
    const body = {
      bpIncomeId: st.bpIncomeId,
      amountMinor: minor,
      frequencyUnit: st.frequencyUnit,
      frequencyNumber: st.frequencyNumber,
    };
    const req$ =
      st.editingId == null
        ? this.budgetPlanningApplicationService.createFundingLine(planId, st.budgetId, body)
        : this.budgetPlanningApplicationService.updateFundingLine(planId, st.budgetId, st.editingId, body);
    req$.pipe(takeUntilDestroyed(this.destroyRef)).subscribe({
      next: () => {
        this.fundingForm.set(null);
        this.reloadEditor(planId);
      },
      error: (err: unknown) => {
        this.fundingForm.set({
          ...st,
          error: readLedgerApiErrorMessage(err, 'Could not save funding line.'),
        });
      },
    });
  }

  deleteFunding(budgetId: number, line: IBpFundingLineResponse): void {
    const planId = this.activePlanId();
    if (!planId) {
      return;
    }
    this.budgetPlanningApplicationService
      .deleteFundingLine(planId, budgetId, line.bpFundingLineId)
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: () => this.reloadEditor(planId),
        error: (err: unknown) =>
          this.actionError.set(readLedgerApiErrorMessage(err, 'Could not delete funding line.')),
      });
  }

  onIncomeAmountInput(ev: Event): void {
    const st = this.incomeForm();
    if (!st || !(ev.target instanceof HTMLInputElement)) {
      return;
    }
    this.patchIncomeForm({ amountDollars: sanitizeMoneyDollarsInput(ev.target.value) });
  }

  onIncomeAmountBlur(): void {
    const st = this.incomeForm();
    if (!st) {
      return;
    }
    this.patchIncomeForm({ amountDollars: formatMoneyDollarsOnBlur(st.amountDollars) });
  }

  onPayableAmountInput(ev: Event): void {
    const st = this.payableForm();
    if (!st || !(ev.target instanceof HTMLInputElement)) {
      return;
    }
    this.patchPayableForm({ amountDollars: sanitizeMoneyDollarsInput(ev.target.value) });
  }

  onPayableAmountBlur(): void {
    const st = this.payableForm();
    if (!st) {
      return;
    }
    this.patchPayableForm({ amountDollars: formatMoneyDollarsOnBlur(st.amountDollars) });
  }

  onFundingAmountInput(ev: Event): void {
    const st = this.fundingForm();
    if (!st || !(ev.target instanceof HTMLInputElement)) {
      return;
    }
    this.patchFundingForm({ amountDollars: sanitizeMoneyDollarsInput(ev.target.value) });
  }

  onFundingAmountBlur(): void {
    const st = this.fundingForm();
    if (!st) {
      return;
    }
    this.patchFundingForm({ amountDollars: formatMoneyDollarsOnBlur(st.amountDollars) });
  }

  moneyKeydown(ev: KeyboardEvent): void {
    onMoneyDollarsKeydown(ev);
  }

  patchIncomeForm(patch: Partial<IIncomeFormState>): void {
    const cur = this.incomeForm();
    if (cur) {
      this.incomeForm.set({ ...cur, ...patch });
    }
  }

  patchPayableForm(patch: Partial<IPayableFormState>): void {
    const cur = this.payableForm();
    if (cur) {
      this.payableForm.set({ ...cur, ...patch });
    }
  }

  patchBudgetForm(patch: Partial<IBudgetFormState>): void {
    const cur = this.budgetForm();
    if (cur) {
      this.budgetForm.set({ ...cur, ...patch });
    }
  }

  patchFundingForm(patch: Partial<IFundingFormState>): void {
    const cur = this.fundingForm();
    if (cur) {
      this.fundingForm.set({ ...cur, ...patch });
    }
  }

  private closeAllForms(): void {
    this.incomeForm.set(null);
    this.payableForm.set(null);
    this.budgetForm.set(null);
    this.fundingForm.set(null);
  }
}

interface IIncomeFundingAllocationView {
  accountId: number;
  budgetAccountDescription: string;
  amountPerIncomePayPeriodMinor: number;
  fundingStatedAmountMinor: number;
  fundingFrequencyLabel: string;
}

interface IIncomeFormState {
  editingId: number | null;
  amountDollars: string;
  frequencyUnit: string;
  frequencyNumber: number;
  accountId: number | null;
  error: string | null;
}

interface IPayableFormState {
  editingId: number | null;
  description: string;
  amountDollars: string;
  frequencyUnit: string;
  frequencyNumber: number;
  budgetAccountId: number | null;
  error: string | null;
}

interface IBudgetFormState {
  editingId: number | null;
  accountId: number | null;
  error: string | null;
}

interface IFundingFormState {
  budgetId: number;
  editingId: number | null;
  bpIncomeId: number | null;
  amountDollars: string;
  frequencyUnit: string;
  frequencyNumber: number;
  error: string | null;
}
