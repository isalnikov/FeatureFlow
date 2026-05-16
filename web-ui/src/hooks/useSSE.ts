import { useEffect, useState } from 'react';

export interface SSEStatus {
  jobId: string;
  phase: string;
  progressPercent: number;
  currentCost: number;
  bestCost: number;
  iteration: number;
}

export function useSSE(url: string | null, service: 'data' | 'planning' = 'data') {
  const [status, setStatus] = useState<SSEStatus | null>(null);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    if (!url) return;

    const baseUrl = service === 'planning'
      ? import.meta.env.VITE_PLANNING_ENGINE_URL || 'http://localhost:8081'
      : import.meta.env.VITE_API_URL || 'http://localhost:8080';

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
  }, [url, service]);

  return { status, error };
}
