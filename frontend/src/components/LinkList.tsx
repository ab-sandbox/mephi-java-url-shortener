import { useState } from "react";
import EditClickLimit from "./EditClickLimit";
import type { LinkResponse } from "../types/links";

interface LinkListProps {
  links: LinkResponse[];
  onUpdated: () => void;
}

function LinkList({ links, onUpdated }: LinkListProps) {
  const [editingCode, setEditingCode] = useState<string | null>(null);

  function handleUpdated() {
    setEditingCode(null);
    onUpdated();
  }

  return (
    <section className="link-list">
      <h2>My links</h2>

      {links.length === 0 ? (
        <p>No links yet.</p>
      ) : (
        <div className="link-items">
          {links.map((link) => (
            <article key={link.code} className="link-item">
              <a href={link.shortUrl} target="_blank" rel="noopener noreferrer">
                {link.shortUrl}
              </a>

              <p className="original-url">{link.originalUrl}</p>

              <p>
                Clicks: {link.clickCount} / {link.clickLimit}
              </p>

              <p>Expires: {new Date(link.expiresAt).toLocaleString("ru-RU")}</p>

              {editingCode === link.code ? (
                <EditClickLimit
                  link={link}
                  onUpdated={handleUpdated}
                  onCancel={() => setEditingCode(null)}
                />
              ) : (
                <div className="link-actions">
                  <button
                    type="button"
                    className="secondary-button"
                    onClick={() => setEditingCode(link.code)}
                  >
                    Edit limit
                  </button>
                </div>
              )}
            </article>
          ))}
        </div>
      )}
    </section>
  );
}

export default LinkList;
