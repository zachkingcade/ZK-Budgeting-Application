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

/** Strip non-numeric (except one decimal) and cap fractional digits to 2 while typing. */
export function sanitizeMoneyDollarsInput(raw: string): string {
  const s: string = (raw ?? '').replace(/[^\d.]/g, '');
  const firstDot: number = s.indexOf('.');
  if (firstDot === -1) {
    return s;
  }
  const intPart: string = s.slice(0, firstDot).replace(/\./g, '');
  const fracPart: string = s.slice(firstDot + 1).replace(/\./g, '').slice(0, 2);
  if (firstDot === 0) {
    return '.' + fracPart;
  }
  return intPart + '.' + fracPart;
}

/** On blur, normalize to two decimal places or clear if invalid / empty. */
export function formatMoneyDollarsOnBlur(raw: string): string {
  const t: string = (raw ?? '').trim();
  if (t === '' || t === '.') {
    return '';
  }
  const normalized: string = t.replace(/,/g, '');
  const parsed: number = Number(normalized);
  if (!Number.isFinite(parsed)) {
    return '';
  }
  return (Math.round(parsed * 100) / 100).toFixed(2);
}

const MONEY_DOLLARS_NAV_KEYS: ReadonlySet<string> = new Set([
  'Backspace',
  'Delete',
  'Tab',
  'Escape',
  'Enter',
  'ArrowLeft',
  'ArrowRight',
  'Home',
  'End',
]);

/** Restrict keyboard input to digits and a single decimal; block a third fractional digit. */
export function onMoneyDollarsKeydown(ev: KeyboardEvent): void {
  if (MONEY_DOLLARS_NAV_KEYS.has(ev.key) || ev.ctrlKey || ev.metaKey || ev.altKey) {
    return;
  }
  const target = ev.target;
  if (!(target instanceof HTMLInputElement)) {
    return;
  }
  const val: string = target.value ?? '';
  const start: number = target.selectionStart ?? 0;
  const end: number = target.selectionEnd ?? 0;

  if (/^\d$/.test(ev.key)) {
    const hypothetical: string = val.slice(0, start) + ev.key + val.slice(end);
    const dot: number = hypothetical.indexOf('.');
    if (dot >= 0) {
      const fracDigits: string = hypothetical.slice(dot + 1).replace(/\D/g, '');
      if (fracDigits.length > 2) {
        ev.preventDefault();
      }
    }
    return;
  }
  if (ev.key === '.' && !val.includes('.')) {
    return;
  }
  ev.preventDefault();
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

