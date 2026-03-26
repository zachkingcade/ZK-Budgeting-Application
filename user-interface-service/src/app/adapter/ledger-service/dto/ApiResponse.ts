import { MetaData } from "./MetaData";

export interface ApiResponse<T> {
  statusMessage: string;
  metaData: MetaData;
  data: T;
}