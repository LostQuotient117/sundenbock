export type SortDir = 'asc' | 'desc';
export type SortKey<T> = `${Extract<keyof T, string>}:${SortDir}`;

export type PageQuery<T> = Partial<Record<Extract<keyof T, string>, string | number>> & {
  search?: string;
  page?: number;
  pageSize?: number;
  sort?: SortKey<T>;  // z.B. "createdAt:desc"
};

export interface Page<T> {
  items: T[];
  total: number;
  page: number;
  pageSize: number;
}
