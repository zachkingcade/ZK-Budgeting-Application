import { MetaDataDto } from './meta-data.dto';

export interface IApiResponseDto<T> {
  statusMessage: string;
  metaData: MetaDataDto;
  data: T;
}

export type ApiResponseDto<T> = IApiResponseDto<T>;
