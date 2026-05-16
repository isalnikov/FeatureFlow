import { format, parseISO, isValid } from 'date-fns';

export function formatDate(date: string | Date): string {
  const d = typeof date === 'string' ? parseISO(date) : date;
  if (!isValid(d)) return 'Invalid date';
  return format(d, 'MMM dd, yyyy');
}

export function formatShortDate(date: string | Date): string {
  const d = typeof date === 'string' ? parseISO(date) : date;
  if (!isValid(d)) return 'Invalid';
  return format(d, 'MMM dd');
}

export function daysBetween(start: string | Date, end: string | Date): number {
  const s = typeof start === 'string' ? parseISO(start) : start;
  const e = typeof end === 'string' ? parseISO(end) : end;
  return Math.ceil((e.getTime() - s.getTime()) / (1000 * 60 * 60 * 24));
}

export function addDaysToDate(date: string | Date, days: number): string {
  const d = typeof date === 'string' ? parseISO(date) : date;
  const result = new Date(d);
  result.setDate(result.getDate() + days);
  return result.toISOString().split('T')[0];
}
