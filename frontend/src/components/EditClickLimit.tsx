import { useState, type FormEvent } from "react";
import { updateClickLimit } from "../api/linkApi";
import { getUserId } from "../utils/userSession";
import type { LinkResponse } from "../types/links";

interface EditClickLimitProps {
  link: LinkResponse;
  onUpdated: () => void;
  onCancel: () => void;
}

function EditClickLimit({ link, onUpdated, onCancel }: EditClickLimitProps) {
  const [clickLimit, setClickLimit] = useState(link.clickLimit);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);

  async function handleSubmit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();

    const userId = getUserId();

    if (!userId) {
      setError("User session not found");
      return;
    }

    setLoading(true);
    setError(null);

    try {
      await updateClickLimit(link.code, clickLimit, userId);
      onUpdated();
    } catch (error) {
      setError(error instanceof Error ? error.message : "Unexpected error");
    } finally {
      setLoading(false);
    }
  }

  return (
    <form className="edit-form" onSubmit={handleSubmit}>
      <div className="form-field">
        <label htmlFor={`click-limit-${link.code}`}>New click limit</label>

        <input
          id={`click-limit-${link.code}`}
          type="number"
          min={Math.max(1, link.clickCount)}
          value={clickLimit}
          onChange={(event) => setClickLimit(Number(event.target.value))}
          required
        />
      </div>

      <div className="edit-actions">
        <button type="submit" disabled={loading}>
          {loading ? "Saving..." : "Save"}
        </button>

        <button
          type="button"
          className="secondary-button"
          onClick={onCancel}
          disabled={loading}
        >
          Cancel
        </button>
      </div>

      {error && <p role="alert">{error}</p>}
    </form>
  );
}

export default EditClickLimit;
