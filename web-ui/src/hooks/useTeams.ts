import { useState, useEffect, useCallback } from 'react';
import { teamsApi } from '../api/teams';
import type { Team } from '../types';

export function useTeams() {
  const [data, setData] = useState<Team[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  const fetchTeams = useCallback(async () => {
    setLoading(true);
    setError(null);
    try {
      const result = await teamsApi.list();
      setData(result);
    } catch (e: unknown) {
      setError(e instanceof Error ? e.message : 'Failed to fetch teams');
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => {
    fetchTeams();
  }, [fetchTeams]);

  return { data, loading, error, refetch: fetchTeams };
}

export function useTeam(id: string | null) {
  const [data, setData] = useState<Team | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    if (!id) {
      setData(null);
      setLoading(false);
      return;
    }

    let cancelled = false;
    setLoading(true);

    teamsApi
      .getById(id)
      .then((result) => {
        if (!cancelled) setData(result);
      })
      .catch((e: unknown) => {
        if (!cancelled) setError(e instanceof Error ? e.message : 'Failed to fetch team');
      })
      .finally(() => {
        if (!cancelled) setLoading(false);
      });

    return () => {
      cancelled = true;
    };
  }, [id]);

  return { data, loading, error };
}
