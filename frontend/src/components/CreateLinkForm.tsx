import { useState, type FormEvent } from "react";
import { createLink } from "../api/linkApi";
import { getUserId, saveUserId } from "../utils/userSession";
import type { LinkResponse } from "../types/links";

function CreateLinkForm() {
  const [originalUrl, setOriginalUrl] = useState("");
  const [clickLimit, setClickLimit] = useState(10);

  const [createdLink, setCreatedLink] = useState<LinkResponse | null>(null);
  const [error, setError] = useState<string | null>(null);
  const [loading, setLoading] = useState(false);

  async function handleSubmit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();

    setLoading(true);
    setError(null);
    setCreatedLink(null);

    try {
      const link = await createLink({ originalUrl, clickLimit }, getUserId());

      saveUserId(link.userId);

      setCreatedLink(link);
      setOriginalUrl("");
    } catch (error) {
      setError(error instanceof Error ? error.message : "Unexpected error");
    } finally {
      setLoading(false);
    }
  }

  return (
    <section>
      <form onSubmit={handleSubmit}>
        <h2>Create short link</h2>

        <div className="form-field">
          <label htmlFor="originalUrl">Original URL</label>
          <input
            id="originalUrl"
            type="url"
            placeholder="https://example.com"
            value={originalUrl}
            onChange={(event) => setOriginalUrl(event.target.value)}
            required
          />
        </div>

        <div className="form-field">
          <label htmlFor="clickLimit">Click limit</label>
          <input
            id="clickLimit"
            type="number"
            min="1"
            value={clickLimit}
            onChange={(event) => setClickLimit(Number(event.target.value))}
            required
          />
        </div>

        <button type="submit" disabled={loading}>
          {loading ? "Creating..." : "Create link"}
        </button>
      </form>

      {error && <p role="alert">{error}</p>}

      {createdLink && (
        <div className="result">
          <h3>Link created</h3>

          <a
            href={createdLink.shortUrl}
            target="_blank"
            rel="noopener noreferrer"
          >
            {createdLink.shortUrl}
          </a>

          <p>Expires: {new Date(createdLink.expiresAt).toLocaleString()}</p>
        </div>
      )}
    </section>
  );
}

export default CreateLinkForm;
