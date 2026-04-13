import { IMetaData } from './MetaData';

export interface ApiResponse<T> {
  statusMessage: string;
  metaData: IMetaData;
  data: T;
}
