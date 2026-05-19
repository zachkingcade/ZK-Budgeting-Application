export interface NotificationModel {
  id: number;
  userId: number;
  datetime: string;
  system: string;
  title: string;
  message: string;
  seen: boolean;
}
