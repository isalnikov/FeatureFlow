import { useEffect, useState, useCallback } from 'react';

export function useSSE(url: string | null) {
  const [status, setStatus] = useState<Record<string, unknown> | null>(null);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    if (!url) return;

    const baseUrl = import.meta.env.VITE_API_URL || 'http://localhost:8080';
    const eventSource = new EventSource(`${baseUrl}${url}`);

    eventSource.onmessage = (event) => {
      try {
        setStatus(JSON.parse(event.data));
      } catch {
        setError('Failed to parse SSE data');
      }
    };

    eventSource.onerror = () => {
      setError('SSE connection lost');
      eventSource.close();
    };

    return () => eventSource.close();
  }, [url]);

  return { status, error };
}
