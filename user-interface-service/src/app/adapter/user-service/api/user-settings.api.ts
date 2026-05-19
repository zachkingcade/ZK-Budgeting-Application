import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { ApiResponseDto } from '../dto/api-response.dto';
import { ICreateUserSettingRequest, IUserSettingDto } from '../dto/user-settings.dto';
import { UserHttpClientService } from '../client/user-http-client.service';

@Injectable({
  providedIn: 'root',
})
export class UserSettingsApi {
  constructor(private readonly client: UserHttpClientService) {}

  listAll(): Observable<ApiResponseDto<IUserSettingDto[]>> {
    return this.client.get<ApiResponseDto<IUserSettingDto[]>>('/user/settings');
  }

  getByName(name: string): Observable<ApiResponseDto<IUserSettingDto>> {
    return this.client.get<ApiResponseDto<IUserSettingDto>>(`/user/settings/by-name/${encodeURIComponent(name)}`);
  }

  create(body: ICreateUserSettingRequest): Observable<ApiResponseDto<IUserSettingDto>> {
    return this.client.post<ApiResponseDto<IUserSettingDto>>('/user/settings', body);
  }

  updateByName(name: string, settingValue: string): Observable<ApiResponseDto<IUserSettingDto>> {
    return this.client.put<ApiResponseDto<IUserSettingDto>>(
      `/user/settings/by-name/${encodeURIComponent(name)}`,
      { settingValue },
    );
  }

  deleteByName(name: string): Observable<ApiResponseDto<void> | null> {
    return this.client.delete<ApiResponseDto<void> | null>(
      `/user/settings/by-name/${encodeURIComponent(name)}`,
    );
  }
}
