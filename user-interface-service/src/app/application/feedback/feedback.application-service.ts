import { Injectable } from '@angular/core';
import { Observable, map } from 'rxjs';
import { UserFeedbackApi } from '../../adapter/user-service/api/user-feedback.api';
import { FeedbackType, IFeedbackDto, ISubmitFeedbackRequest } from '../../adapter/user-service/dto/feedback.dto';
import { FeedbackModel } from '../../domain/feedback/feedback.model';

@Injectable({
  providedIn: 'root',
})
export class FeedbackApplicationService {
  constructor(private readonly api: UserFeedbackApi) {}

  submit(type: FeedbackType, title: string, message: string): Observable<FeedbackModel> {
    const body: ISubmitFeedbackRequest = { type, title, message };
    return this.api.submit(body).pipe(map((res) => this.toModel(res.data!)));
  }

  list(type: FeedbackType): Observable<FeedbackModel[]> {
    return this.api.list(type).pipe(map((res) => (res.data ?? []).map((dto) => this.toModel(dto))));
  }

  markSeen(id: number): Observable<FeedbackModel> {
    return this.api.markSeen(id).pipe(map((res) => this.toModel(res.data!)));
  }

  delete(id: number): Observable<void> {
    return this.api.delete(id).pipe(map(() => undefined));
  }

  private toModel(dto: IFeedbackDto): FeedbackModel {
    return {
      id: dto.id,
      type: dto.type,
      title: dto.title,
      message: dto.message,
      userId: dto.userId,
      username: dto.username,
      createdAt: dto.createdAt,
      seen: dto.seen,
    };
  }
}
