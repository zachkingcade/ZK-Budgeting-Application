import { FeedbackType } from '../../adapter/user-service/dto/feedback.dto';

export interface FeedbackModel {
  id: number;
  type: FeedbackType;
  title: string;
  message: string;
  userId: number;
  username: string;
  createdAt: string;
  seen: boolean;
}
