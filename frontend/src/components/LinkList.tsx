import type { LinkResponse } from "../types/links";

interface LinkListProps {
  links: LinkResponse[];
}

function LinkList({ links }: LinkListProps) {
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
            </article>
          ))}
        </div>
      )}
    </section>
  );
}

export default LinkList;
