export interface IMetaData {
  requestDate: string;
  requestTime: string;
  executionTimeMs?: number;
  dataResponseCount?: number;
}

export type MetaData = IMetaData;