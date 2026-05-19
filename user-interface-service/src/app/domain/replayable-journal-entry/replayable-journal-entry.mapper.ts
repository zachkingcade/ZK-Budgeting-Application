import {
  IReplayableJournalEntryDetail,
  IReplayableJournalEntryLine,
} from '../../adapter/ledger-service/dto/replayable-journal-entry/replayable-journal-entry.dto';
import { JournalLineDTOEnrichedResponse } from '../../adapter/ledger-service/dto/journal-entry/JournalLineDTOEnrichedResponse';
import {
  dollarsStringToMinorUnits,
  IJournalEntryLineDraft,
  minorUnitsToDollarsString,
} from '../journal-entry/journal-entry.validation';

export function replayableLinesToDrafts(lines: IReplayableJournalEntryLine[]): IJournalEntryLineDraft[] {
  return lines.map((line) => ({
    amountDollars: minorUnitsToDollarsString(line.amount),
    accountId: line.accountId,
    direction: line.direction,
    notes: '',
  }));
}

export function replayableDetailToDrafts(detail: IReplayableJournalEntryDetail): IJournalEntryLineDraft[] {
  return replayableLinesToDrafts(detail.lines ?? []);
}

export function enrichedJournalLinesToDrafts(
  lines: JournalLineDTOEnrichedResponse[],
): IJournalEntryLineDraft[] {
  return lines.map((line) => ({
    amountDollars: minorUnitsToDollarsString(line.amount),
    accountId: line.accountId,
    direction: line.direction === 'C' ? 'C' : 'D',
    notes: '',
  }));
}

export interface IIncomeFundingReplayableAllocation {
  accountId: number;
  amountPerIncomePayPeriodMinor: number;
}

/** One debit line per funded budget account (per income payment); user adds credits when loading. */
export function incomeFundingAllocationsToReplayableDebitDrafts(
  allocations: IIncomeFundingReplayableAllocation[],
): IJournalEntryLineDraft[] {
  return allocations
    .filter((a) => a.amountPerIncomePayPeriodMinor !== 0)
    .map((a) => ({
      amountDollars: minorUnitsToDollarsString(a.amountPerIncomePayPeriodMinor),
      accountId: a.accountId,
      direction: 'D' as const,
      notes: '',
    }));
}

export function draftsToReplayableLines(drafts: IJournalEntryLineDraft[]): IReplayableJournalEntryLine[] {
  const result: IReplayableJournalEntryLine[] = [];
  for (const draft of drafts) {
    const amount = dollarsStringToMinorUnits(draft.amountDollars);
    if (amount == null || draft.accountId == null) {
      continue;
    }
    if (draft.direction !== 'C' && draft.direction !== 'D') {
      continue;
    }
    result.push({
      amount,
      accountId: draft.accountId,
      direction: draft.direction,
    });
  }
  return result;
}
