export interface INotificationDto {
  id: number;
  userId: number;
  datetime: string;
  system: string;
  title: string;
  message: string;
  seen: boolean;
}

export interface IClearNotificationsRequest {
  ids: number[];
}
