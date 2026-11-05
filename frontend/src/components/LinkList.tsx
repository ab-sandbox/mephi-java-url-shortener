import LinkItem from "./LinkItem";
import type { LinkResponse } from "../types/links";

interface LinkListProps {
  links: LinkResponse[];
  onUpdated: () => void;
}

function LinkList({ links, onUpdated }: LinkListProps) {
  return (
    <section className="link-list">
      <div className="list-header">
        <h2>My links</h2>
        <span>{links.length} links</span>
      </div>

      {links.length === 0 ? (
        <p>No links yet. Create your first short link above.</p>
      ) : (
        <div className="link-items">
          {links.map((link) => (
            <LinkItem key={link.code} link={link} onChanged={onUpdated} />
          ))}
        </div>
      )}
    </section>
  );
}

export default LinkList;
