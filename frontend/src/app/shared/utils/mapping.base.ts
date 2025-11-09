export type Mapper<I, O> = (input: I) => O;

export const mapPage = <I, O>(
  src: { items: I[]; total: number; page: number; pageSize: number },
  itemMap: Mapper<I, O>
) => ({
  ...src,
  items: src.items.map(itemMap),
});

export const compose = <A, B, C>(ab: Mapper<A, B>, bc: Mapper<B, C>): Mapper<A, C> =>
  (a: A) => bc(ab(a));
