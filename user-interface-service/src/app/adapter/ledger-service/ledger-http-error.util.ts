import { HttpErrorResponse } from '@angular/common/http';

function messageFromUnknownBody(body: unknown): string | null {
  if (body == null) {
    return null;
  }
  if (typeof body === 'string') {
    const t = body.trim();
    if (!t) {
      return null;
    }
    try {
      const parsed: unknown = JSON.parse(t) as unknown;
      return messageFromUnknownBody(parsed);
    } catch {
      return t;
    }
  }
  if (typeof body !== 'object') {
    return null;
  }
  const o = body as Record<string, unknown>;
  const direct = o['message'];
  if (typeof direct === 'string' && direct.trim().length > 0) {
    return direct.trim();
  }
  const nested = o['error'];
  if (typeof nested === 'string' && nested.trim().length > 0) {
    return nested.trim();
  }
  if (nested && typeof nested === 'object') {
    const em = (nested as Record<string, unknown>)['message'];
    if (typeof em === 'string' && em.trim().length > 0) {
      return em.trim();
    }
  }
  return null;
}

/** Reads ledger `ApiErrorResponse` or common Spring error JSON from an HttpErrorResponse. */
export function readLedgerApiErrorMessage(err: unknown, fallback: string): string {
  if (!(err instanceof HttpErrorResponse)) {
    return fallback;
  }
  const fromBody = messageFromUnknownBody(err.error);
  if (fromBody) {
    return fromBody;
  }
  if (typeof err.message === 'string' && err.message.trim().length > 0 && !err.message.startsWith('Http failure response')) {
    return err.message.trim();
  }
  return fallback;
}
