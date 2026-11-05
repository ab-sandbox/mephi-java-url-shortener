import type { CreateLinkRequest, LinkResponse } from "../types/links";

const API_BASE_URL = "/api/links";

export async function createLink(
  request: CreateLinkRequest,
  userId: string | null,
): Promise<LinkResponse> {
  const headers: Record<string, string> = {
    "Content-Type": "application/json",
  };

  if (userId) {
    headers["X-User-Id"] = userId;
  }

  const response = await fetch(API_BASE_URL, {
    method: "POST",
    headers,
    body: JSON.stringify(request),
  });

  if (!response.ok) {
    throw new Error(`Failed to create link: HTTP ${response.status}`);
  }

  return response.json() as Promise<LinkResponse>;
}
