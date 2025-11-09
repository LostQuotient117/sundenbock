export type MapFn<I, O> = (input: I) => O;

export type Keep<K extends PropertyKey = PropertyKey> = { kind: 'keep'; from?: K };
export type OmitField = { kind: 'omit' };
export type Rename<K extends PropertyKey> = { kind: 'rename'; from: K };

// NEU: from? auch bei map/array/nested erlaubt
export type MapField<I, O, FK extends PropertyKey = PropertyKey> = {
  kind: 'map';
  from?: FK;
  map: MapFn<I, O>;
};

export type MapArray<I, O, FK extends PropertyKey = PropertyKey> = {
  kind: 'array';
  from?: FK;
  map: MapFn<I, O>;
};

export type MapNested<I, O, S extends MapperSpec<I, O>, FK extends PropertyKey = PropertyKey> = {
  kind: 'nested';
  from?: FK;
  spec: S;
};

export type FieldRule<I, O, K extends keyof I = keyof I> =
  | Keep<K>
  | OmitField
  | Rename<K>
  | (K extends keyof I ? MapField<I[K], any, K> : MapField<any, any, K>)
  | MapArray<any, any, K>
  | MapNested<any, any, any, K>;

// Spezifikation: Zielkeys -> Regel
export type MapperSpec<I, O> = {
  [K in keyof O]:
    | FieldRule<I, O, keyof I>
    // „smart default“: gleicher Name => keep
    | (K extends keyof I ? Keep<K> | undefined : never);
};

// ==== 2) Ableitung des Outputtyps aus Spec (Type-Level) ====================

type RuleOutput<I, O, R> =
  R extends Keep<infer FK> ? (FK extends keyof I ? I[FK] : never) :
  R extends Rename<infer FK> ? (FK extends keyof I ? I[FK] : never) :
  R extends MapField<infer _In, infer Out, any> ? Out :
  R extends MapArray<infer _In, infer Out, any> ? Out[] :
  R extends MapNested<infer _In, infer Out, any, any> ? Out :
  R extends undefined ? never : never;

export type OutputFromSpec<I, S extends MapperSpec<I, any>> = {
  [K in keyof S]: RuleOutput<I, any, S[K]>;
};

// Convenience: Builder mit Typ-Inferenz
export const defineMapper = <I, O>() =>
  <S extends MapperSpec<I, O>>(spec: S) => spec;

// ==== 3) Runtime-Mapper (Engine) ===========================================

export function mapBySpec<I, S extends MapperSpec<I, any>>(
  input: I,
  spec: S
): OutputFromSpec<I, S> {
  const out: any = {};
  for (const k in spec) {
    const rule = spec[k];
    if (!rule) continue;

    switch (rule.kind) {
      case 'keep': {
        const from = (rule.from ?? k) as keyof I;
        out[k] = (input as any)[from];
        break;
      }
      case 'rename': {
        const from = rule.from as keyof I;
        out[k] = (input as any)[from];
        break;
      }
      case 'omit': {
        break;
      }
      case 'map': {
        const from = (rule.from ?? k) as keyof I;
        out[k] = rule.map((input as any)[from]);
        break;
      }
      case 'array': {
        const from = (rule.from ?? k) as keyof I;
        const src = (input as any)[from] as any[];
        out[k] = Array.isArray(src) ? src.map(rule.map) : [];
        break;
      }
      case 'nested': {
        const from = (rule.from ?? k) as keyof I;
        const src = (input as any)[from];
        out[k] = src == null ? src : mapBySpec(src, rule.spec as any);
        break;
      }
      default: {
        const _exhaustive: never = rule;
        throw new Error('Unknown rule: ' + String(_exhaustive));
      }
    }
  }
  return out as OutputFromSpec<I, S>;
}

// ===== Bonus: nützliche Helfer, falls du sie hier sammeln willst ===========

// sehr simple Heuristik für ISO-ähnliche Strings (Type-Level + Guard)
export type ISODateString = `${number}-${number}-${number}T${string}`;
export const toDate = (s: string | null | undefined) => (s ? new Date(s) : null);
export function isISODateString(x: unknown): x is ISODateString {
  return typeof x === 'string' && /\d{4}-\d{2}-\d{2}T/.test(x);
}

// snake_case -> camelCase (Type-Level + flache Laufzeitvariante)
type CamelCase<S extends string> =
  S extends `${infer H}_${infer T}` ? `${Lowercase<H>}${Capitalize<CamelCase<T>>}` : Lowercase<S>;

export type DeepCamelKeys<T> = T extends any[]
  ? DeepCamelKeys<T[number]>[]
  : T extends object
    ? { [K in keyof T as K extends string ? CamelCase<K> : K]: DeepCamelKeys<T[K]> }
    : T;

export function camelKeys<T extends Record<string, any>>(obj: T) {
  const out: any = {};
  for (const k in obj) {
    const ck = k.replace(/_([a-z])/g, (_, c) => c.toUpperCase());
    out[ck] = obj[k];
  }
  return out as DeepCamelKeys<T>;
}
