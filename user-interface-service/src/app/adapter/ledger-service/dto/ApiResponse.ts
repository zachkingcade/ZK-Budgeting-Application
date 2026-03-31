import { MetaData } from "./MetaData";

export interface IApiResponse<T> {
  statusMessage: string;
  metaData: MetaData;
  data: T;
}

export type ApiResponse<T> = IApiResponse<T>;