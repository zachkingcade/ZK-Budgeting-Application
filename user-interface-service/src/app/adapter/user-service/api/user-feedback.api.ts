import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { ApiResponseDto } from '../dto/api-response.dto';
import { FeedbackType, IFeedbackDto, ISubmitFeedbackRequest } from '../dto/feedback.dto';
import { UserHttpClientService } from '../client/user-http-client.service';

@Injectable({
  providedIn: 'root',
})
export class UserFeedbackApi {
  constructor(private readonly client: UserHttpClientService) {}

  submit(body: ISubmitFeedbackRequest): Observable<ApiResponseDto<IFeedbackDto>> {
    return this.client.post<ApiResponseDto<IFeedbackDto>>('/user/feedback', body);
  }

  list(type: FeedbackType): Observable<ApiResponseDto<IFeedbackDto[]>> {
    return this.client.get<ApiResponseDto<IFeedbackDto[]>>(`/user/feedback?type=${type}`);
  }

  markSeen(id: number): Observable<ApiResponseDto<IFeedbackDto>> {
    return this.client.patch<ApiResponseDto<IFeedbackDto>>(`/user/feedback/${id}/seen`, {});
  }

  delete(id: number): Observable<ApiResponseDto<void> | null> {
    return this.client.delete<ApiResponseDto<void> | null>(`/user/feedback/${id}`);
  }
}
