import type { NotificationResponse } from "../types/notifications";

const API_BASE_URL = "/api/notifications";

export async function getNotifications(
  userId: string,
): Promise<NotificationResponse[]> {
  const response = await fetch(API_BASE_URL, {
    headers: {
      "X-User-Id": userId,
    },
  });

  if (!response.ok) {
    throw new Error(`Failed to load notifications: HTTP ${response.status}`);
  }

  return response.json() as Promise<NotificationResponse[]>;
}

export async function getUnreadNotifications(
  userId: string,
): Promise<NotificationResponse[]> {
  const response = await fetch(`${API_BASE_URL}/unread`, {
    headers: {
      "X-User-Id": userId,
    },
  });

  if (!response.ok) {
    throw new Error(
      `Failed to load unread notifications: HTTP ${response.status}`,
    );
  }

  return response.json() as Promise<NotificationResponse[]>;
}

export async function markNotificationAsRead(
  id: number,
  userId: string,
): Promise<NotificationResponse> {
  const response = await fetch(`${API_BASE_URL}/${id}/read`, {
    method: "PATCH",
    headers: {
      "X-User-Id": userId,
    },
  });

  if (!response.ok) {
    throw new Error(
      `Failed to mark notification as read: HTTP ${response.status}`,
    );
  }

  return response.json() as Promise<NotificationResponse>;
}
