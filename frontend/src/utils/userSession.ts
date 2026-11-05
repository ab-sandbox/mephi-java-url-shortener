const USER_ID_KEY = "url-shortener-user-id";

export function getUserId(): string | null {
  return localStorage.getItem(USER_ID_KEY);
}

export function saveUserId(userId: string): void {
  localStorage.setItem(USER_ID_KEY, userId);
}
