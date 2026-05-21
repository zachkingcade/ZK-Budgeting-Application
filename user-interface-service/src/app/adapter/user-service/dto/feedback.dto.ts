export type FeedbackType = 'bug' | 'suggestion';

export interface ISubmitFeedbackRequest {
  type: FeedbackType;
  title: string;
  message: string;
}

export interface IFeedbackDto {
  id: number;
  type: FeedbackType;
  title: string;
  message: string;
  userId: number;
  username: string;
  createdAt: string;
  seen: boolean;
}
