export interface IJournalEntryLineDraft {
  amountDollars: string;
  accountId: number | null;
  direction: 'C' | 'D' | '';
  notes: string;
}

export interface IValidationResult {
  valid: boolean;
  errorMessage: string | null;
}

export function dollarsStringToMinorUnits(amountDollars: string): number | null {
  const trimmed: string = (amountDollars ?? '').trim();
  if (trimmed.length === 0) {
    return null;
  }
  const normalized: string = trimmed.replace(/,/g, '');
  const parsed: number = Number(normalized);
  if (!Number.isFinite(parsed)) {
    return null;
  }
  const minorUnits: number = Math.round(parsed * 100);
  return minorUnits;
}

export function validateJournalEntryDraft(lines: IJournalEntryLineDraft[]): IValidationResult {
  if (!lines || lines.length < 2) {
    return { valid: false, errorMessage: 'Journal entry requires at least two lines.' };
  }

  let credit: number = 0;
  let debit: number = 0;

  for (const line of lines) {
    if (line.accountId == null) {
      return { valid: false, errorMessage: 'Each line requires an account.' };
    }
    if (line.direction !== 'C' && line.direction !== 'D') {
      return { valid: false, errorMessage: 'Each line requires a direction (Credit or Debit).' };
    }

    const minorUnits: number | null = dollarsStringToMinorUnits(line.amountDollars);
    if (minorUnits == null) {
      return { valid: false, errorMessage: 'Each line requires a valid amount.' };
    }
    if (minorUnits < 1) {
      return { valid: false, errorMessage: 'Each line requires a positive, non-zero amount.' };
    }

    if (line.direction === 'C') {
      credit += minorUnits;
    } else {
      debit += minorUnits;
    }
  }

  if (credit !== debit) {
    return { valid: false, errorMessage: 'Credit and debit totals must match.' };
  }

  return { valid: true, errorMessage: null };
}

