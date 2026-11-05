import { useEffect, useState } from "react";
import {
  getNotifications,
  markNotificationAsRead,
} from "../api/notificationApi";
import { getUserId } from "../utils/userSession";
import type { NotificationResponse } from "../types/notifications";

function NotificationList() {
  const [notifications, setNotifications] = useState<NotificationResponse[]>(
    [],
  );
  const [loading, setLoading] = useState(true);
  const [markingId, setMarkingId] = useState<number | null>(null);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    let cancelled = false;

    async function loadNotifications() {
      const userId = getUserId();

      if (!userId) {
        setLoading(false);
        return;
      }

      try {
        const result = await getNotifications(userId);

        if (!cancelled) {
          setNotifications(result);
          setError(null);
        }
      } catch (error) {
        if (!cancelled) {
          setError(
            error instanceof Error
              ? error.message
              : "Failed to load notifications",
          );
        }
      } finally {
        if (!cancelled) {
          setLoading(false);
        }
      }
    }

    void loadNotifications();

    return () => {
      cancelled = true;
    };
  }, []);

  const unreadCount = notifications.filter(
    (notification) => notification.readAt === null,
  ).length;

  async function handleMarkAsRead(id: number) {
    const userId = getUserId();

    if (!userId) {
      setError("User session not found");
      return;
    }

    setMarkingId(id);
    setError(null);

    try {
      const updated = await markNotificationAsRead(id, userId);

      setNotifications((current) =>
        current.map((notification) =>
          notification.id === id ? updated : notification,
        ),
      );
    } catch (error) {
      setError(
        error instanceof Error
          ? error.message
          : "Failed to mark notification as read",
      );
    } finally {
      setMarkingId(null);
    }
  }

  return (
    <section className="notification-list">
      <div className="list-header">
        <h2>Notifications</h2>
        <span>{unreadCount} unread</span>
      </div>

      {error && <p role="alert">{error}</p>}

      {loading ? (
        <p>Loading notifications...</p>
      ) : notifications.length === 0 ? (
        <p>No notifications yet.</p>
      ) : (
        <div className="notification-items">
          {notifications.map((notification) => (
            <article
              key={notification.id}
              className={`notification-item ${
                notification.readAt === null ? "unread" : ""
              }`}
            >
              <div className="notification-header">
                <strong>{notification.type}</strong>

                <span>
                  {new Date(notification.createdAt).toLocaleString("ru-RU")}
                </span>
              </div>

              <p>{notification.message}</p>

              <div className="notification-footer">
                <span>Link: {notification.shortCode}</span>

                {notification.readAt === null ? (
                  <button
                    type="button"
                    className="secondary-button"
                    onClick={() => void handleMarkAsRead(notification.id)}
                    disabled={markingId === notification.id}
                  >
                    {markingId === notification.id
                      ? "Saving..."
                      : "Mark as read"}
                  </button>
                ) : (
                  <span className="notification-read">Read</span>
                )}
              </div>
            </article>
          ))}
        </div>
      )}
    </section>
  );
}

export default NotificationList;
