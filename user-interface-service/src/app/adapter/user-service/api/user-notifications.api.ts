import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { ApiResponseDto } from '../dto/api-response.dto';
import { IClearNotificationsRequest, INotificationDto } from '../dto/notification.dto';
import { UserHttpClientService } from '../client/user-http-client.service';

@Injectable({
  providedIn: 'root',
})
export class UserNotificationsApi {
  constructor(private readonly client: UserHttpClientService) {}

  list(limit?: number): Observable<ApiResponseDto<INotificationDto[]>> {
    const query = limit != null ? `?limit=${limit}` : '';
    return this.client.get<ApiResponseDto<INotificationDto[]>>(`/user/notifications${query}`);
  }

  markSeen(id: number): Observable<ApiResponseDto<INotificationDto>> {
    return this.client.patch<ApiResponseDto<INotificationDto>>(`/user/notifications/${id}/seen`, {});
  }

  delete(id: number): Observable<ApiResponseDto<void> | null> {
    return this.client.delete<ApiResponseDto<void> | null>(`/user/notifications/${id}`);
  }

  clear(ids: number[]): Observable<ApiResponseDto<void>> {
    const body: IClearNotificationsRequest = { ids };
    return this.client.post<ApiResponseDto<void>>('/user/notifications/clear', body);
  }

  clearAll(): Observable<ApiResponseDto<void> | null> {
    return this.client.delete<ApiResponseDto<void> | null>('/user/notifications/clearall');
  }
}
