import { useEffect, useState } from "react";
import { getUserLinks } from "./api/linkApi";
import CreateLinkForm from "./components/CreateLinkForm";
import LinkList from "./components/LinkList";
import { getUserId } from "./utils/userSession";
import type { LinkResponse } from "./types/links";

function App() {
  const [links, setLinks] = useState<LinkResponse[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [refreshKey, setRefreshKey] = useState(0);

  useEffect(() => {
    let cancelled = false;

    async function loadLinks() {
      const userId = getUserId();

      if (!userId) {
        setLinks([]);
        setError(null);
        setLoading(false);
        return;
      }

      try {
        const result = await getUserLinks(userId);

        if (!cancelled) {
          setLinks(result);
          setError(null);
        }
      } catch (error) {
        if (!cancelled) {
          setError(
            error instanceof Error ? error.message : "Failed to load links",
          );
        }
      } finally {
        if (!cancelled) {
          setLoading(false);
        }
      }
    }

    void loadLinks();

    return () => {
      cancelled = true;
    };
  }, [refreshKey]);

  function refreshLinks() {
    setLoading(true);
    setRefreshKey((current) => current + 1);
  }

  return (
    <main className="app">
      <h1>URL Shortener</h1>
      <p>Manage your short links in one place.</p>

      <CreateLinkForm onCreated={refreshLinks} />

      {error && <p role="alert">{error}</p>}

      {loading && <p role="status">Refreshing links...</p>}

      <LinkList links={links} onUpdated={refreshLinks} />
    </main>
  );
}

export default App;
