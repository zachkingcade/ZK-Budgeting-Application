export interface IMetaDataDto {
  requestDate: string;
  requestTime: string;
  executionTimeMs?: number;
  dataResponseCount?: number;
}

export type MetaDataDto = IMetaDataDto;
