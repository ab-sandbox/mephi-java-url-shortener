import { useEffect, useState } from "react";
import { deleteLink } from "../api/linkApi";
import { getUserId } from "../utils/userSession";
import type { LinkResponse } from "../types/links";
import EditClickLimit from "./EditClickLimit";

interface LinkItemProps {
  link: LinkResponse;
  onChanged: () => void;
}

function LinkItem({ link, onChanged }: LinkItemProps) {
  const [editing, setEditing] = useState(false);
  const [deleting, setDeleting] = useState(false);
  const [copied, setCopied] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [now, setNow] = useState<number | null>(null);

  useEffect(() => {
    const updateTime = () => setNow(Date.now());

    const timeoutId = window.setTimeout(updateTime, 0);
    const intervalId = window.setInterval(updateTime, 60_000);

    return () => {
      window.clearTimeout(timeoutId);
      window.clearInterval(intervalId);
    };
  }, []);

  const expired = now !== null && new Date(link.expiresAt).getTime() <= now;

  const limitReached = link.clickCount >= link.clickLimit;
  const active = !expired && !limitReached;

  function handleUpdated() {
    setEditing(false);
    onChanged();
  }

  async function handleCopy() {
    try {
      await navigator.clipboard.writeText(link.shortUrl);
      setCopied(true);
      setError(null);
    } catch {
      setError("Failed to copy link");
    }
  }

  async function handleDelete() {
    if (!window.confirm("Delete this short link?")) {
      return;
    }

    const userId = getUserId();

    if (!userId) {
      setError("User session not found");
      return;
    }

    setDeleting(true);
    setError(null);

    try {
      await deleteLink(link.code, userId);
      onChanged();
    } catch (error) {
      setError(
        error instanceof Error ? error.message : "Failed to delete link",
      );
    } finally {
      setDeleting(false);
    }
  }

  return (
    <article className="link-item">
      <div className="link-header">
        <a href={link.shortUrl} target="_blank" rel="noopener noreferrer">
          {link.shortUrl}
        </a>

        <span className={`link-status ${active ? "active" : "inactive"}`}>
          {expired ? "Expired" : limitReached ? "Limit reached" : "Active"}
        </span>
      </div>

      <p className="original-url">{link.originalUrl}</p>

      <div className="link-meta">
        <span>
          Clicks: {link.clickCount} / {link.clickLimit}
        </span>

        <span>Expires: {new Date(link.expiresAt).toLocaleString("ru-RU")}</span>
      </div>

      {editing ? (
        <EditClickLimit
          link={link}
          onUpdated={handleUpdated}
          onCancel={() => setEditing(false)}
        />
      ) : (
        <div className="link-actions">
          <button
            type="button"
            className="secondary-button"
            onClick={() => void handleCopy()}
          >
            {copied ? "Copied!" : "Copy"}
          </button>

          <button
            type="button"
            className="secondary-button"
            onClick={() => setEditing(true)}
          >
            Edit limit
          </button>

          <button
            type="button"
            className="danger-button"
            onClick={() => void handleDelete()}
            disabled={deleting}
          >
            {deleting ? "Deleting..." : "Delete"}
          </button>
        </div>
      )}

      {error && <p role="alert">{error}</p>}
    </article>
  );
}

export default LinkItem;
