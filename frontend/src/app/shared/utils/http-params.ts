import { HttpParams } from '@angular/common/http';

export function buildHttpParams(obj?: Record<string, any>): HttpParams {
  let p = new HttpParams();
  if (!obj) return p;
  for (const [k, v] of Object.entries(obj)) {
    if (v === null || v === undefined || v === '') continue;
    p = p.set(k, String(v));
  }
  return p;
}