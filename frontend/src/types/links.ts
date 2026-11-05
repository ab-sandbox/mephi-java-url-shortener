export interface CreateLinkRequest {
  originalUrl: string;
  clickLimit: number;
}

export interface LinkResponse {
  code: string;
  shortUrl: string;
  originalUrl: string;
  clickLimit: number;
  clickCount: number;
  expiresAt: string;
  userId: string;
}
