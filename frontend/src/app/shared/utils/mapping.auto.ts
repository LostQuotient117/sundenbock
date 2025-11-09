export const isIso = (s: string) => /\d{4}-\d{2}-\d{2}T/.test(s);

export function autoMap<T extends Record<string, any>>(input: T): any {
  if (Array.isArray(input)) return input.map(autoMap);
  if (input && typeof input === 'object') {
    const out: any = {};
    for (const k in input) {
      const v = input[k];
      const ck = k.replace(/_([a-z])/g, (_, c) => c.toUpperCase());
      if (typeof v === 'string' && isIso(v)) out[ck] = new Date(v);
      else if (Array.isArray(v)) out[ck] = v.map(autoMap);
      else if (v && typeof v === 'object') out[ck] = autoMap(v);
      else out[ck] = v;
    }
    return out;
  }
  return input;
}
