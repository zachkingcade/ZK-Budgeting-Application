export interface ISortObject<T = string> {
  type: T;
  direction?: string;
}

export type SortObject<T = string> = ISortObject<T>;
