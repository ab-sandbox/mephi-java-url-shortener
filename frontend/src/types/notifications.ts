export interface NotificationResponse {
  id: number;
  shortCode: string;
  type: string;
  message: string;
  createdAt: string;
  readAt: string | null;
}
